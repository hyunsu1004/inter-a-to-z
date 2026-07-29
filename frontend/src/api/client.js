import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const TOKEN_KEY = 'inter_a2z_token'
const REFRESH_KEY = 'inter_a2z_refresh_token'
const USER_KEY = 'inter_a2z_user'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function clearSessionAndRedirect() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

// 여러 요청이 동시에 401을 맞아도 리프레시 호출은 한 번만 나가도록 대기열로 관리
let isRefreshing = false
let pendingQueue = []

function resolveQueue(newToken) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (newToken) resolve(newToken)
    else reject(new Error('refresh-failed'))
  })
  pendingQueue = []
}

apiClient.interceptors.response.use(
  (res) => res,
  async (err) => {
    const { response, config } = err
    const isAuthEndpoint = config?.url?.includes('/api/auth/')

    if (response?.status !== 401 || isAuthEndpoint || config._retried) {
      if (response?.status === 401 && isAuthEndpoint) {
        clearSessionAndRedirect()
      }
      return Promise.reject(err)
    }

    const refreshToken = localStorage.getItem(REFRESH_KEY)
    if (!refreshToken) {
      clearSessionAndRedirect()
      return Promise.reject(err)
    }

    config._retried = true

    if (isRefreshing) {
      // 이미 진행 중인 리프레시가 끝날 때까지 대기했다가 새 토큰으로 재시도
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      }).then((newToken) => {
        config.headers.Authorization = `Bearer ${newToken}`
        return apiClient(config)
      })
    }

    isRefreshing = true
    try {
      const { data } = await axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken })
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(REFRESH_KEY, data.refreshToken)
      resolveQueue(data.token)
      config.headers.Authorization = `Bearer ${data.token}`
      return apiClient(config)
    } catch (refreshErr) {
      resolveQueue(null)
      clearSessionAndRedirect()
      return Promise.reject(refreshErr)
    } finally {
      isRefreshing = false
    }
  }
)

export default apiClient
