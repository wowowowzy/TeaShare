/**
 * 后端 API 基地址。
 *
 * 默认留空 => 请求使用相对路径 /api/...：
 *   - 生产环境：由 Nginx 把 /api 反向代理到后端，前后端同域，
 *     既避免跨域（CORS），也让验证码等基于 Session Cookie 的接口能正常工作。
 *   - 本地开发：由 vite.config.js 的 server.proxy 代理到本机后端。
 *
 * 如需直连后端（例如不配代理时），在 .env.local 设置：
 *   VITE_API_BASE=http://127.0.0.1:8888
 */
export const API_BASE = import.meta.env.VITE_API_BASE ?? ''
