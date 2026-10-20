/**
 * 集中管理 localStorage 键名，避免散落各处。
 */
export const STORAGE_KEYS = {
  token: 'token',
  userInfo: 'userInfo',
  sessionId: 'sessionId',
  avatarTimestamp: 'avatarTimestamp',
  likedComments: 'liked_comments'
}

export function readJSON(key, fallback = null) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

export function writeJSON(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    /* 忽略写入失败（如隐私模式） */
  }
}

export function readString(key, fallback = '') {
  return localStorage.getItem(key) ?? fallback
}

export function writeString(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch {
    /* 忽略 */
  }
}

export function remove(keys) {
  const list = Array.isArray(keys) ? keys : [keys]
  list.forEach((k) => {
    try {
      localStorage.removeItem(k)
    } catch {
      /* 忽略 */
    }
  })
}
