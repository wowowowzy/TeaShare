import axios from 'axios'
import JSONBig from 'json-bigint'
import { ElMessage } from 'element-plus'
import { API_BASE } from '../utils/apiBase'
import { STORAGE_KEYS } from '../utils/storage'

/**
 * json-bigint：后端雪花算法 ID 是 19 位 Long，超出 JS Number.MAX_SAFE_INTEGER，
 * 普通 JSON.parse 会丢精度。storeAsString 把所有大整数转成字符串。
 */
const jsonBig = JSONBig({ storeAsString: true })

const request = axios.create({
  baseURL: API_BASE,
  timeout: 100000,
  withCredentials: true,
  transformResponse: [
    function (data) {
      if (typeof data !== 'string') return data
      try {
        return jsonBig.parse(data)
      } catch {
        // 可能是纯文本（如 401 的「未登录，请先登录」）
        return data
      }
    }
  ]
})

/* ---------------------------------------------------------------------------
 * 统一契约（重要）：
 *   成功（code === 1 / 200）→ resolve 出 `data` 本体；data 为 null 时 resolve 出整个 envelope。
 *   失败（code 为其它值）   → reject(new ApiError(msg))，并弹一次错误提示。
 *
 * 因此 **调用方只判断 Promise 成功/失败，绝不再判断 res.code**。
 * （旧项目拦截器剥离 code 后组件仍判断 res.code，导致恒为 false 的 bug。）
 * ------------------------------------------------------------------------- */

export class ApiError extends Error {
  constructor(message, code) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.token)
    if (token) config.headers['token'] = `${token}`

    const sessionId = localStorage.getItem(STORAGE_KEYS.sessionId)
    if (sessionId) config.headers['sessionId'] = `${sessionId}`

    config.withCredentials = true
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    // 二进制响应（验证码图片等）直接返回
    const rt = response.config.responseType
    if (rt === 'blob' || rt === 'arraybuffer') return response.data

    const res = response.data

    // 纯文本响应（AI 流式 / 部分接口）直接返回
    if (typeof res === 'string') return res

    if (res && (res.code === 1 || res.code === 200)) {
      return res.data !== undefined && res.data !== null ? res.data : res
    }

    const msg = (res && res.msg) || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new ApiError(msg, res && res.code))
  },
  async (error) => {
    const cfg = error.config || {}
    const url = cfg.url || ''
    const status = error.response?.status

    const isAiApi = url.includes('/api/user/ai/')
    const isLogoutRequest = url.includes('/logout')
    const isGetUserInfo = url.includes('/api/user/show')

    if (error.response) {
      switch (status) {
        case 401:
          if (isAiApi) {
            ElMessage.error('AI 接口未授权，请重新登录')
          } else if (isGetUserInfo) {
            // 保留本地用户信息，不强制登出（避免刷新即掉线）
            ElMessage.error('获取用户信息失败，已保留本地用户信息')
          } else {
            const { emitUnauthorized } = await import('../utils/authBus')
            emitUnauthorized()
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          if (!isLogoutRequest) ElMessage.error('请求的资源不存在')
          break
        case 413:
          ElMessage.error('文件大小超出限制')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.msg || error.response.statusText || '网络错误')
      }
    } else if (error.message) {
      ElMessage.error(`服务器暂时无法响应：${error.message}`)
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

export default request
