/**
 * 未授权（401）事件总线。
 *
 * request.js 不直接 import store / router，避免与 stores/user.js 形成循环依赖：
 * 由 main.js 在应用启动时注册处理器，拦截器只负责发出事件。
 */
let handler = null

export function onUnauthorized(fn) {
  handler = fn
}

export function emitUnauthorized() {
  if (handler) handler()
}
