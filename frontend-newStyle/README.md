# 茗承古韵 · 用户端前端

「茗承古韵」茶文化平台用户端。基于 **Vue 3 + Vite + Tailwind CSS 4 + Element Plus**，视觉风格为 **Editorial（编辑杂志风）**。

---

## 技术栈

| 组件 | 说明 |
|---|---|
| Vue 3 | `<script setup>` 组合式 API |
| Vite 6 | 构建与开发服务器 |
| Tailwind CSS 4 | 设计令牌 + 工具类（`@theme` / `@layer`） |
| Element Plus | **仅用于复杂控件**（弹窗 / 上传 / 分页 / 图片预览），已做单色直角主题覆盖 |
| Pinia | 用户状态 |
| axios + json-bigint | 请求封装（雪花 ID 精度保护） |
| three.js | 3D 茶具（GLTF + Draco） |

---

## 快速开始

```bash
npm install
npm run dev      # http://localhost:5173/tea/
npm run build    # 产物输出到 dist/
npm run preview  # 预览构建产物
```

> 开发服务器会把 `/api` 代理到本机后端 `http://127.0.0.1:8888`（见 `vite.config.js`），
> 因此本地联调无需配置跨域。

---

## 部署约定

用户端部署在 **`https://wowowzy.art/tea/`** 子目录下，因此：

1. `vite.config.js` 的 `base` 必须是 `/tea/`（由 `VITE_BASE` 控制）；
2. 前端所有接口请求走**相对路径**，前缀 `/tea`（`VITE_API_BASE=/tea`），
   由 Nginx 同域反代到后端，既无跨域，又让验证码依赖的 Session Cookie 成为第一方 Cookie；
3. 静态资源统一用 `asset()`（`src/utils/apiBase.js`）拼接 `BASE_URL`，
   **禁止**在 JS 里硬编码 `/assest/...` 绝对路径，否则子目录部署会 404。

Nginx 关键配置（完整版见 `../deploy/nginx-wowowzy.art.conf`）：

```nginx
location /tea/ { alias /www/wwwroot/tea-web/; try_files $uri $uri/ /tea/index.html; }
location /tea/api/ {
    proxy_pass http://127.0.0.1:8888/api/;
    proxy_buffering off;          # SSE（AI 流式）必须关闭缓冲
    proxy_read_timeout 300s;
}
```

---

## 目录结构

```
src/
├── api/                    # 接口封装
│   ├── request.js          # axios 实例 + 统一契约拦截器
│   ├── user.js             # 用户 / 验证码 / 订单 / 地址 / 优惠券
│   ├── goods.js            # 商品 / 秒杀 / 评论
│   ├── cart.js             # 购物车 / 下单
│   ├── community.js        # 茶友社
│   ├── ai.js               # AI 对话（SSE）
│   └── quiz.js             # 趣味闯关
├── components/
│   ├── layout/             # AppNavbar / AppFooter
│   ├── ui/                 # SectionLabel / SmartImage / EmptyState / SkeletonBlock / StarRating
│   ├── home/               # VideoHero / HomeSidebar / TeaVarieties / TeaMaking / TeaHistory
│   └── shop/               # ProductCard / CommentSection / QuizChallenge / SeckillModal / AddressFormDialog
├── content/                # 硬编码内容数据（JSON + 映射常量）
├── router/                 # 路由表（全部懒加载）
├── stores/user.js          # Pinia 用户 Store
├── styles/main.css         # 设计系统 + Element Plus 主题覆盖
├── utils/
│   ├── apiBase.js          # API_BASE / ASSET_BASE / asset()
│   ├── authBus.js          # 401 事件总线（解除循环依赖）
│   └── storage.js          # localStorage 键名集中管理
└── views/                  # 各页面
```

---

## 设计系统（Editorial）

**核心约束**（详见仓库根目录 `前端风格prompt.md`）：

- 配色：暖米色底 `#F9F8F6` + 柔和黑字 `#1C1C1C`，**纯单色**，无彩色强调色；
- 层次：仅用透明度 `/60`（次要）、`/40`（辅助）、`/10`（边框）构建灰度；
- 字体：标题 `font-serif`（衬线，字重 400 不加粗），正文 `font-sans`，标签 `text-xs tracking-[0.2em] uppercase`；
- 形状：**直角**（`rounded-none`）、**无阴影**（`shadow-none`）、无渐变；
- 间距：Section `py-24 md:py-40`，容器 `max-w-7xl px-6 md:px-12`；
- 交互：`hover-underline` 下划线、`group-hover:italic`、图片 `scale-105 duration-1000`。

设计令牌定义在 `src/styles/main.css` 的 `@theme`，通用类在 `@layer components`
（`.btn-editorial` / `.card-editorial` / `.field-input` / `.eyebrow` / `.link-underline` 等）。

---

## 关键实现约定

### 1. 统一 API 契约

`api/request.js` 拦截器在 `code === 1 | 200` 时 **resolve 出 `data` 本体**，
失败时 **reject `ApiError`** 并弹一次提示。

> **调用方只判断 Promise 成功/失败，绝不再判断 `res.code`。**
> 旧项目因拦截器剥离 `code` 后组件仍判断 `res.code`（恒为 false）导致多处 bug，此处已统一。

### 2. 大整数 ID

后端雪花 ID 为 19 位 Long，超出 `Number.MAX_SAFE_INTEGER`。
`json-bigint({ storeAsString: true })` 把大整数转为字符串，避免精度丢失。

### 3. AI 流式对话

`api/ai.js` 用 `fetch` + `ReadableStream` 手动解析 SSE（去掉 `data:` 前缀），
逐块回调实现首字即渲染；请求头携带 `token`，图片走 `multipart`。

### 4. XSS 防护

所有 `v-html` 位置（AI 消息、帖子正文）**先做 HTML 转义**再渲染 Markdown 子集；
朝代历史等硬编码富文本改为**结构化数据 + 组件渲染**，不再 `v-html`。

### 5. 401 处理

拦截器不直接 import store/router（避免循环依赖），通过 `utils/authBus.js` 发事件，
由 `main.js` 注册处理器统一清理登录态并跳转登录页。
`/api/user/ai/**` 与 `/api/user/show` 的 401 只提示、不强制登出。

---

## 后端接口

完整接口契约见仓库 `docs/前端重写规格文档.md` 第 7 章。后端为 Spring Boot（端口 8888），
关键约定：

- 请求头鉴权字段名是 **`token`**（不是 `Authorization`）；
- 验证码依赖 `HttpSession`（Cookie 名 `TEA_SESSION`），图片与校验两个接口**必须同一会话**；
- 业务失败时 HTTP 状态码仍是 200，需以 `code` 判断；但 401 返回**纯文本**而非 JSON。

---

## 静态资源

图片 / 视频位于 `public/assest/` 与 `public/teacher/`，
3D 模型 `public/tea.glb`（40MB，同源加载，避免 OSS 未开放 CORS 导致失败）。
