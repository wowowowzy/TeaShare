import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  login as loginApi,
  register as registerApi,
  getUserShow as getUserShowApi,
  logout as logoutApi
} from '../api/user'
import { STORAGE_KEYS, readJSON, readString, writeJSON, writeString, remove } from '../utils/storage'

export const useUserStore = defineStore('user', () => {
  /* ------------------------------ state ------------------------------ */
  const token = ref(readString(STORAGE_KEYS.token, ''))
  const userInfo = ref(readJSON(STORAGE_KEYS.userInfo, {}) || {})
  const sessionId = ref(readString(STORAGE_KEYS.sessionId, ''))

  /* ------------------------------ getters ------------------------------ */
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const userId = computed(() => userInfo.value?.userId || userInfo.value?.id || '')
  const getSessionId = computed(() => sessionId.value || '')

  /* ------------------------------ helpers ------------------------------ */
  function persist() {
    writeString(STORAGE_KEYS.token, token.value)
    writeJSON(STORAGE_KEYS.userInfo, userInfo.value)
    writeString(STORAGE_KEYS.sessionId, sessionId.value)
  }

  function buildUserInfo(raw, fallbackId = '') {
    const id = raw?.userId || raw?.id || fallbackId || ''
    return {
      ...raw,
      userId: id,
      id,
      username: raw?.username || raw?.userName || raw?.name || '',
      avatar: raw?.avatar ? String(raw.avatar).replace(/`/g, '') : ''
    }
  }

  /* ------------------------------ actions ------------------------------ */

  /** 登录成功返回 true */
  async function login({ username, password }) {
    const res = await loginApi(username, password)
    if (res && res.token) {
      token.value = res.token
      userInfo.value = buildUserInfo(res)
      sessionId.value = res.sessionId || ''
      persist()
      return true
    }
    return false
  }

  /** 注册（拦截器已统一契约：成功即 resolve，失败会 reject） */
  async function register({ username, password, phone }) {
    await registerApi(String(username), String(password), String(phone))
    ElMessage.success('注册成功')
    return true
  }

  async function getUserShow() {
    const res = await getUserShowApi()
    if (!res) return false
    userInfo.value = buildUserInfo(res, userInfo.value?.userId)
    if (res.token) token.value = res.token
    if (res.sessionId) sessionId.value = res.sessionId
    persist()
    return true
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      /* 退出接口失败也继续清理本地状态 */
    }
    clearUserInfo()
    ElMessage.success('退出登录成功')
  }

  function updateAvatar(url) {
    userInfo.value = { ...userInfo.value, avatar: url }
    persist()
  }

  function setSessionId(id) {
    sessionId.value = id || ''
    writeString(STORAGE_KEYS.sessionId, sessionId.value)
  }

  function clearUserInfo() {
    token.value = ''
    userInfo.value = {}
    sessionId.value = ''
    remove([STORAGE_KEYS.token, STORAGE_KEYS.userInfo, STORAGE_KEYS.sessionId])
  }

  return {
    token,
    userInfo,
    sessionId,
    isLoggedIn,
    userName,
    avatar,
    userId,
    getSessionId,
    login,
    register,
    getUserShow,
    logout,
    updateAvatar,
    setSessionId,
    clearUserInfo
  }
})
