import request from './request'
import { API_BASE } from '../utils/apiBase'

/**
 * 后端全局异常处理器返回的错误体形如 {"code":0,"msg":"..."}；
 * 流式接口的兜底事件形如 {"type":"error","code":0,"msg":"..."}。
 * 两者都识别，返回可读 msg；不是错误体则返回 null。
 */
function parseBackendError(text) {
  if (!text) return null
  const t = String(text).trim()
  if (!t.startsWith('{')) return null
  try {
    const obj = JSON.parse(t)
    if (!obj || typeof obj !== 'object') return null
    // 流式兜底事件
    if (obj.type === 'error') return obj.msg || 'AI 服务暂时不可用，请稍后重试'
    // 普通接口错误体（code !== 1）
    if (typeof obj.code === 'number' && obj.code !== 1 && obj.code !== 200) {
      return obj.msg || 'AI 服务异常'
    }
  } catch {
    /* 不是完整 JSON，忽略 */
  }
  return null
}

/**
 * SSE 流式对话。
 *
 * 后端 `POST /api/user/ai/generateStream` 返回 text/event-stream 的原始文本流
 * （Flux<String>，不包 Result）。这里用 fetch + ReadableStream 手动解析，
 * 逐块回调 onChunk，保证首字到达即渲染。
 *
 * ⚠️ 上游大模型异常时（如 Connection reset），Spring 的全局异常处理器会把
 * `{"code":0,"msg":"未知异常"}` 这个 JSON **写进同一条 SSE 流**。若直接当文本
 * 渲染，用户就会看到一串裸 JSON。因此这里累积后校验，识别为错误体时抛出可读错误。
 *
 * @param {string}      message
 * @param {string|number} sessionId
 * @param {File|null}   image
 * @param {(chunk:string)=>void} onChunk
 * @param {()=>void}    [onComplete]
 * @param {(e:Error)=>void} [onError]
 */
export async function generateStream(message, sessionId, image = null, onChunk, onComplete, onError) {
  try {
    const url = `${API_BASE}/api/user/ai/generateStream`
    const headers = {}
    const token = localStorage.getItem('token')
    if (token) headers['token'] = token

    let body
    if (image instanceof File) {
      const formData = new FormData()
      formData.append('message', message ?? '')
      formData.append('sessionId', sessionId)
      formData.append('image', image)
      body = formData
    } else {
      headers['Content-Type'] = 'application/x-www-form-urlencoded'
      const params = new URLSearchParams()
      params.append('message', message ?? '')
      params.append('sessionId', sessionId)
      body = params
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body,
      credentials: 'include'
    })

    if (!response.ok) {
      // 非 2xx 时后端也可能返回 Result JSON，尽量取出 msg
      const errText = await response.text().catch(() => '')
      const backendMsg = parseBackendError(errText)
      throw new Error(backendMsg || `AI 服务请求失败（HTTP ${response.status}）`)
    }
    if (!response.body) {
      throw new Error('当前环境不支持流式响应')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let received = ''
    let receivedSent = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        onComplete && onComplete()
        break
      }
      const chunk = decoder.decode(value, { stream: true })
      // 去掉 SSE 的 `data:` 前缀，过滤空行
      const cleaned = chunk
        .split('\n')
        .map((line) => line.replace(/^data:/, ''))
        .join('')
      if (cleaned.length === 0) continue

      if (!receivedSent) {
        // 尚未开始渲染：先累积，用来判断整段流是否为后端错误体。
        // 阈值取小一点，既够识别 JSON 错误体，又不明显拖慢首字显示。
        received += cleaned
        const looksLikeJson = received.trim().startsWith('{')
        const asError = looksLikeJson ? parseBackendError(received) : null
        // 明确不是错误体，或累积已足够判断 → 开始渲染
        if (asError) {
          // 是错误体：继续累积到流结束，不渲染
          continue
        }
        if (!looksLikeJson || received.length >= 120) {
          receivedSent = true
          onChunk && onChunk(received)
        }
        continue
      }

      onChunk && onChunk(cleaned)
    }

    // 全程未渲染：要么是错误体，要么是很短的正常回复
    if (!receivedSent) {
      const backendMsg = parseBackendError(received)
      if (backendMsg) {
        throw new Error(backendMsg)
      }
      if (received.trim()) {
        receivedSent = true
        onChunk && onChunk(received)
      }
    }
  } catch (error) {
    onError && onError(error)
    throw error
  }
}

/** 历史会话：返回 Map<sessionId, History[]>（后端按 sessionId 分组） */
export const getHistories = () => request({ url: '/api/user/ai/getHistories', method: 'get' })

/** 新建会话，返回雪花 sessionId */
export const createNewSession = () => request({ url: '/api/user/ai/newSession', method: 'get' })
