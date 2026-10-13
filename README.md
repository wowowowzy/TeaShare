# TeaShare

茶叶社区 + 商城（Spring Boot 3.5 + Spring AI）。除常规的电商与论坛能力外，项目重点实现了 **AI 对话（RAG + 长短记忆）** 与 **基于用户行为画像的社区帖子推荐**。

---

## 技术栈

| 组件 | 版本 / 说明 |
|---|---|
| Spring Boot | 3.5.6 |
| Spring AI | 1.1.8 |
| JDK | 17 |
| MySQL | 8.x（需支持 ngram 全文索引，用于 BM25） |
| Redis | **redis-stack-server**（必须含 RediSearch，向量检索依赖 `FT.*`） |
| RabbitMQ | 3.13（需 `x-delayed-message` 延迟插件，用于秒杀/社区异步加权） |
| 大模型 | OpenAI 兼容接口（百炼 MaaS，对话 / 向量 / 重排三类模型） |
| 持久层 | MyBatis + PageHelper |

---

## 快速开始

### 1. 启动依赖服务

```bash
# MySQL 8
docker run -d --name mysql8 -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=<your_password> \
  --restart unless-stopped mysql:8

# Redis（必须是 redis-stack-server，否则没有 RediSearch）
docker run -d --name redis -p 6379:6379 \
  --restart unless-stopped redis/redis-stack-server

# RabbitMQ（需带 x-delayed-message 插件，请使用自建镜像）
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  --restart unless-stopped <your-rabbitmq-delayed-image>:3.13-management
```

### 2. 初始化数据库

```bash
docker exec -i mysql8 mysql -uroot -p<your_password> < src/main/resources/tea.sql
```

`tea.sql` 已包含 `CREATE DATABASE tea` / `USE tea`，可独立导入；其中 `rag_knowledge` 建了 **ngram 全文索引**以支撑 BM25 召回。默认库名为 `tea`。

### 3. 配置环境变量

```bash
cp env.example .env
# 编辑 .env，填入你的真实配置
```

`application.yml` 通过 `spring.config.import: optional:file:./.env[.properties]` 加载，因此**必须从项目根目录启动**。`.env` 已在 `.gitignore` 中，不会被提交。

### 4. 启动

```bash
mvn spring-boot:run          # 开发
mvn -DskipTests package && java -jar target/tea-0.0.1-SNAPSHOT.jar   # 打包
```

启动后访问 `http://127.0.0.1:8888`。

> 首次启动会自动初始化知识库（切片写入 Redis 向量库 + MySQL `rag_knowledge` 双写）与商品数据。知识库以内容哈希作为文档 id，重复启动不会产生脏数据。
>
> 清空 Redis 数据时**不要使用 `FLUSHDB`/`FLUSHALL`** —— RediSearch 索引仅存在于 DB0，而 `FLUSHDB` 会删掉全部索引，导致向量检索不可用（重启应用可自动重建）。

---

## 模块说明

### AI 对话（`/api/user/ai`）

采用 Spring AI Advisor 链编排，职责清晰分层：

| 能力 | 实现 |
|---|---|
| 短期记忆 | `MessageChatMemoryAdvisor` + 自定义 `MyBatisChatMemoryRepository`，落库 `sys_history` |
| 长期记忆 | 独立 Redis 向量库 + `VectorStoreChatMemoryAdvisor`，按 `conversationId` 过滤召回 |
| 人设与知识 | `RagSystemPromptAdvisor` —— 检索结果写入 **system prompt**，不污染用户消息 |
| RAG 检索 | 向量召回 + MySQL ngram **BM25** → **RRF 融合** → `qwen3.7-text-rerank` 重排 |
| 降级策略 | 重排失败自动退回召回顺序；检索无结果时正常对话 |
| 工具调用 | Function Calling（茶叶信息、快递查询等） |

关键接口：

- `POST /generateStream` —— SSE 流式对话，表单参数 `message` / `sessionId` / `image`（可选，多模态）
- `GET /getHistories?userId=` —— 按会话分组的历史
- `GET /newSession` —— 生成新会话 id

### 社区推荐（`/api/user/community`）

异步行为加权 + 画像召回，权重按「帖子」粒度聚合：

```
用户行为 → RabbitMQ → 消费者累积画像 → 召回 → 重排 → 返回帖子
```

| 行为 | 权重 |
|---|---|
| 浏览帖子 | +1 |
| 点赞 / 取消点赞 | ±3 |
| 评论 / 删除评论 | ±5 |
| 收藏 / 取消收藏 | ±10 |
| 点赞评论 | 不参与加权 |

- 画像结构：`userRecommend:{userId}` 为 ZSET，**member = postId**、score = 权重累加（TTL 30 分钟）。以帖子为粒度，避免不同帖子抽到同一关键词时权重互相稀释。
- 帖子关键词由大模型异步抽取，缓存于 `postWord:{postId}`，作为召回的语义 query。
- 召回：取画像 Top 帖子 → 关键词向量召回 → 按 **postId 精确回表**（不做 title LIKE 扫描）→ 空画像时返回空列表。

---

## 项目结构

```
src/main/java/com/example/tea
├── ai/                     # AI 能力（RAG / 记忆 / 重排 / 社区关键词抽取）
│   ├── rag/                #   RagSystemPromptAdvisor、BM25、RRF 混合检索
│   ├── memory/             #   MyBatisChatMemoryRepository
│   ├── rerank/             #   DashScope 原生重排调用
│   └── community/          #   CommunityKeywordExtractor
├── config/                 # AiConfig / RagConfig / RedisVectorStoreConfig / InitConfig
├── controller/             # 接口层
├── service/impl/           # 业务实现（AI 对话、社区、商城、秒杀等）
├── mapper/                 # MyBatis Mapper
└── rabbitmq/               # 交换机、队列与消费者
src/main/resources
├── application.yml         # 配置（凭据全部走环境变量）
├── tea.sql                 # 完整建库脚本（含 rag_knowledge 全文索引）
├── mapper/                 # Mapper XML
└── knowledge/              # 知识库原始文本
```

---

## 注意事项

- **凭据一律通过 `.env` 注入**，`application.yml` 只保留 `${ENV}` 占位符，请勿硬编码密钥。
- `application.yml` 中 `MYSQL_PASSWORD:123456`、`RABBITMQ_PASSWORD:guest` 仅为本地开发默认值，生产环境务必由 `.env` 覆盖。
- 向量模型维度与索引强相关，更换 embedding 模型需重建向量库。
- Redis 中的 ZSET member 由 Jackson 序列化，用 `redis-cli` 查看时可能带引号（如 `"12"`），属正常现象。