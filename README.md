# 茗承古韵 · TeaShare

> 一个把「广东茶文化」做成可交互体验的全栈项目：**3D 茶具 · AI 茶艺顾问 · 茶文化商城 · 茶友社区**。

光看名字容易以为又是个普通电商 Demo。实际上这里塞了几件事：

- **3D 茶具**：Three.js 渲染潮汕工夫茶具，8 个器物可点击查看材质与历史渊源；
- **AI 小茗**：基于 RAG 的茶文化问答，能记住你聊过什么，还能看图；
- **行为画像推荐**：你在社区里点过的赞、收藏过的帖，会异步沉淀成画像，反哺「猜你喜欢」；
- **完整电商链路**：商品、购物车、下单、优惠券、秒杀（RabbitMQ 延迟队列 + Redis 预扣库存）。

后端 Spring Boot 3.5 + Spring AI，前端 Vue 3 + Vite，约 6900 行 Java / 44 个前端源文件。

---

## 目录

- [快速开始](#快速开始)
- [它解决了什么问题](#它解决了什么问题)
- [AI 对话是怎么搭的](#ai-对话是怎么搭的)
- [社区推荐是怎么算的](#社区推荐是怎么算的)
- [项目结构](#项目结构)
- [接口概览](#接口概览)
- [踩过的坑](#踩过的坑)
- [注意事项](#注意事项)

---

## 快速开始

### 一、起依赖服务

三个服务都是**硬要求**，缺一个就跑不起来：

```bash
# MySQL 8 —— rag_knowledge 需要 ngram 全文索引（BM25 召回依赖它）
docker run -d --name mysql8 -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=<your_password> \
  --restart unless-stopped mysql:8

# Redis —— 必须是 redis-stack-server，普通 redis 没有 RediSearch，向量检索会直接失败
docker run -d --name redis -p 6379:6379 \
  --restart unless-stopped redis/redis-stack-server

# RabbitMQ —— 需要 x-delayed-message 延迟插件（秒杀超时取消、社区异步加权都靠它）
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  --restart unless-stopped <your-rabbitmq-delayed-image>:3.13-management
```

### 二、导入数据库

```bash
docker exec -i mysql8 mysql -uroot -p<your_password> < src/main/resources/tea.sql
```

`tea.sql` 自带 `CREATE DATABASE tea` / `USE tea`，可以独立执行；`rag_knowledge` 表已建好 **ngram 全文索引**。

### 三、配置环境变量

```bash
cp env.example .env
# 然后填真实配置
```

配置文件里全是 `${ENV}` 占位符，凭据只走 `.env`（已在 `.gitignore` 里）。

> ⚠️ `application.yml` 用的是 `spring.config.import: optional:file:./.env[.properties]`，
> 所以**必须从项目根目录启动**，换个目录就读不到 `.env`。

### 四、启动

```bash
mvn spring-boot:run                                                 # 开发
mvn -DskipTests package && java -jar target/tea-0.0.1-SNAPSHOT.jar  # 打包
```

跑起来后访问 `http://127.0.0.1:8888`。

**首次启动会自动做三件事**，不用手动准备数据：

1. 把 `RAGKnowledge.txt` 切片，双写进 Redis 向量库 + MySQL `rag_knowledge`；
2. 商品与秒杀库存预热进 Redis；
3. 已有帖子抽取关键词、建推荐向量。

知识库文档 id 取内容哈希，所以**重复启动不会产生重复数据**。

### 五、前端（可选）

```bash
cd frontend && npm install && npm run dev
```

---

## 它解决了什么问题

这几个点值得单独说，因为它们不是"CRUD 堆出来的"：

| 问题 | 处理方式 |
|---|---|
| 大模型不知道广东茶的具体知识 | RAG 混合检索，把检索结果写进 **system prompt** 而非用户消息，避免污染对话 |
| 模型答完就忘 | 短期记忆（窗口 + 落库）+ 长期记忆（独立向量库），两层记忆分离 |
| 社区帖子质量参差、用户找不到想看的 | 行为加权画像 + 语义召回 + 重排 |
| 秒杀高并发下超卖 | Redis 预扣库存 + RabbitMQ 延迟队列异步落库 |
| 雪花 ID 超出 JS 精度 | 前端用 `json-bigint` 把 Long 转字符串 |

---

## AI 对话是怎么搭的

入口在 `/api/user/ai`，用 Spring AI 的 **Advisor 链**编排，各司其职：

```
用户消息
   │
   ├─ MessageChatMemoryAdvisor   短期记忆：注入历史，存回 sys_history
   ├─ RagSystemPromptAdvisor     知识增强：向量 + BM25 → RRF → 重排 → 写 system prompt
   └─ VectorStoreChatMemoryAdvisor 长期记忆：跨会话语义召回
   │
   └─> 流式返回（SSE）
```

| 能力 | 实现要点 |
|---|---|
| 短期记忆 | `MessageWindowChatMemory` + 自定义 `MyBatisChatMemoryRepository`，落库 `sys_history` |
| 长期记忆 | 独立 Redis 向量库，按 `conversationId` 过滤，避免串会话 |
| RAG | 向量召回 ∥ MySQL ngram BM25 → **RRF 融合** → `qwen3.7-text-rerank` 精排 |
| 降级 | 重排失败自动退回召回顺序；检索为空时照常对话，不报错 |
| 多模态 | `image` 走 multipart，与文本一起送入模型 |
| 工具调用 | Function Calling：茶叶信息查询、快递查询 |

**接口：**

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/generateStream` | SSE 流式对话（`message` / `sessionId` / `image?`） |
| GET | `/getHistories` | 按会话分组的历史（身份只从 token 解析） |
| GET | `/newSession` | 生成新会话 id |

---

## 社区推荐是怎么算的

不是简单按热度排序，而是**按用户行为异步累积画像再召回**：

```
用户行为 → RabbitMQ → 消费者累积画像 → 关键词向量召回 → 重排 → 帖子列表
```

**权重表**（按「帖子」粒度聚合，不是按关键词）：

| 行为 | 权重 |
|---|---|
| 浏览帖子 | +1 |
| 点赞 / 取消点赞 | ±3 |
| 评论 / 删除评论 | ±5 |
| 收藏 / 取消收藏 | ±10 |
| 点赞评论 | 不计入 |

**几个刻意的设计：**

- 画像存成 ZSET `userRecommend:{userId}`，**member 是 postId**、score 是权重累加，TTL 30 分钟。
  用帖子做粒度可以避免「两个帖子恰好抽到同一关键词，权重互相稀释」。
- 帖子关键词由大模型**异步**抽取，缓存到 `postWord:{postId}`，作为召回的语义 query。
- 召回时取画像 Top 帖子 → 关键词向量召回 → **按 postId 精确回表**（不做 `title LIKE` 全表扫）。
- 画像为空时直接返回空列表，不硬凑推荐。

---

## 项目结构

```
src/main/java/com/example/tea
├── ai/
│   ├── rag/          RagSystemPromptAdvisor · 混合检索(向量+BM25) · RRF · 重排
│   ├── memory/       MyBatisChatMemoryRepository（对话记忆落库）
│   ├── rerank/       DashScope 原生重排调用
│   └── community/    CommunityKeywordExtractor（帖子关键词抽取）
├── config/           AiConfig · RagConfig · RedisVectorStoreConfig · InitConfig · 全局异常
├── controller/
│   ├── user/         用户端：商品 / 购物车 / 订单 / 社区 / AI / 秒杀 …
│   └── admin/        管理端：商品 / 订单 / 用户 / 日志 / 数据大屏
├── service/impl/     业务实现
├── mapper/           MyBatis Mapper
├── rabbitmq/         秒杀 · 社区画像 · 优惠券延迟队列（producer + consumer）
└── FunctionCalling/  TeaTool · QueryExpressTool

src/main/resources
├── application.yml       全部凭据走环境变量
├── tea.sql               完整建库脚本（含 ngram 全文索引）
├── RAGKnowledge.txt      知识库原始文本
└── mapper/               Mapper XML

frontend/                 用户端（Vue 3 + Vite）
frontend-admin/           管理端
deploy/                   Nginx 配置（前后端同域部署）
```

---

## 接口概览

统一响应体：

```json
{ "code": 1, "msg": "success", "data": {} }
```

> 业务失败时 **HTTP 仍是 200**，前端要以 `code` 判定。
> 例外：未登录返回 **401 + 纯文本**（不是 JSON），需单独处理。

认证走请求头 **`token`**（不是 `Authorization`）；分页参数 `page` / `pageSize`（默认 1 / 10）。

主要模块：用户与验证码、商品与评论、购物车、订单、地址、优惠券、问答闯关、秒杀、文件上传、社区、AI 对话。

---

## 踩过的坑

这些都是实际调试中踩出来的，写下来省得重复踩：

**1. Redis 不能用 `FLUSHDB` / `FLUSHALL`**

RediSearch 索引只存在于 DB0，而 `FLUSHDB` 会把索引一起删掉，向量检索直接失效。
真清空了也不用慌，**重启应用会自动重建**。

**2. Redis 必须是 `redis-stack-server`**

官方 `redis` 镜像没有 RediSearch，`FT.*` 命令不存在，向量检索起不来。

**3. AI 流式接口的异常必须自己兜**

上游大模型偶发 `Connection reset` 时，异常会冒到全局异常处理器——
但那个处理器是给普通 JSON 接口写的，会把 `{"code":0,"msg":"未知异常"}` **写进已提交的 SSE 流**，
前端只能收到一坨裸 JSON。

所以 `ChatController` 在 `.content()` 之后用 `onErrorResume` 兜底，转成结构化事件：

```
{"type":"error","code":0,"msg":"AI 服务连接被重置，请重试"}
```

另外**参数绑定阶段**的异常（如 `sessionId=abc`）发生在 Controller 之前，`onErrorResume` 够不着，
得在 `GlobalExceptionHandler` 里按 `Accept: text/event-stream` 分流。

**4. 连接池的空闲连接会被对端单向关闭**

`HttpClientConfig` 里做了 `validateAfterInactivity(20s)` + `evictIdleConnections(30s)`，
复用前校验活性，能明显减少 `Connection reset`。

**5. `userId` 不要从请求参数取**

`/ai/getHistories` 只从 token 解析身份。旧写法收 `?userId=` 参数，等于允许任何人读别人聊天记录。

---

## 注意事项

- **凭据一律走 `.env`**，`application.yml` 只有 `${ENV}` 占位符，别硬编码密钥。
- `application.yml` 里的 `MYSQL_PASSWORD:123456`、`RABBITMQ_PASSWORD:guest` 只是本地默认值，生产必须覆盖。
- **换 embedding 模型要重建向量库**：索引维度与模型强绑定。
- 用 `redis-cli` 看 ZSET 时 member 可能带引号（如 `"12"`），这是 Jackson 序列化的正常表现。
- 前端部署在 `/tea/` 子目录，接口经 Nginx 同域反代（既避开跨域，也让验证码的 Session Cookie 成为第一方 Cookie）。配置见 `deploy/`。

---

## License

仅用于学习与技术交流。