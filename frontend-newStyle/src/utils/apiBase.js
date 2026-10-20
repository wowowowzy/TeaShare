/**
 * 后端 API 基地址。
 *
 * - 生产环境：`VITE_API_BASE=/tea` → 请求走 `/tea/api/...`，由 Nginx 同域反代到后端，
 *   既无跨域，又让验证码依赖的 Session Cookie 成为第一方 Cookie。
 * - 本地开发：留空 → 请求走 `/api/...`，由 vite.config.js 的 server.proxy 转发到 8888。
 */
export const API_BASE = import.meta.env.VITE_API_BASE ?? ''

/** 静态资源基路径（生产为 '/tea/'），用于拼接 public/assest 下的图片 */
export const ASSET_BASE = import.meta.env.BASE_URL || '/'

/**
 * 把内容数据里的站点根绝对路径（如 /assest/绿茶/绿1.jpg）
 * 拼成带 base 的可用地址。已经是 http(s) 外链或 data: 的原样返回。
 */
export function asset(path) {
  if (!path) return ''
  if (/^(https?:)?\/\//.test(path) || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }
  return `${ASSET_BASE.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}
