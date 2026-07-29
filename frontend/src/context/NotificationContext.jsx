import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import { useAuth } from './AuthContext.jsx'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const TOKEN_KEY = 'inter_a2z_token'

const NotificationContext = createContext(null)

let toastSeq = 0

// 브라우저 기본 EventSource는 Authorization 헤더를 못 붙이기 때문에(쿼리 파라미터로 토큰을 노출하는
// 흔한 우회는 이 프로젝트의 인증 보안 방향과 맞지 않다고 판단해 피함), fetch + ReadableStream으로
// text/event-stream 응답을 직접 파싱한다. 백엔드: NotificationController#stream 참고.
function parseSseChunk(chunk) {
  let eventName = 'message'
  let data = ''
  for (const line of chunk.split('\n')) {
    if (line.startsWith('event:')) eventName = line.slice(6).trim()
    else if (line.startsWith('data:')) data += line.slice(5).trim()
  }
  return { eventName, data }
}

export function NotificationProvider({ children }) {
  const { user } = useAuth()
  const [toasts, setToasts] = useState([])
  const [lastEvent, setLastEvent] = useState(null)
  const retryTimerRef = useRef(null)

  const dismissToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const pushEvent = useCallback((event) => {
    const id = ++toastSeq
    setToasts((prev) => [...prev, { id, ...event }])
    setLastEvent({ id, ...event })
    setTimeout(() => dismissToast(id), 5000)
  }, [dismissToast])

  useEffect(() => {
    if (!user) return undefined

    let stopped = false
    const controller = new AbortController()

    async function connect() {
      const token = localStorage.getItem(TOKEN_KEY)
      if (!token || stopped) return

      try {
        const res = await fetch(`${API_BASE_URL}/api/notifications/stream`, {
          headers: { Authorization: `Bearer ${token}` },
          signal: controller.signal,
        })
        if (!res.ok || !res.body) throw new Error('stream connection failed')

        const reader = res.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (!stopped) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          const chunks = buffer.split('\n\n')
          buffer = chunks.pop()
          for (const chunk of chunks) {
            if (!chunk.trim()) continue
            const { eventName, data } = parseSseChunk(chunk)
            if (eventName === 'notification' && data) {
              try {
                pushEvent(JSON.parse(data))
              } catch {
                // 잘못된 형식의 이벤트는 무시하고 계속 스트림을 읽는다
              }
            }
          }
        }
      } catch {
        // 네트워크 문제, 탭 백그라운드 전환 등으로 스트림이 끊길 수 있음 - 아래 finally에서 재연결
      } finally {
        if (!stopped) {
          retryTimerRef.current = setTimeout(connect, 5000)
        }
      }
    }

    connect()

    return () => {
      stopped = true
      controller.abort()
      if (retryTimerRef.current) clearTimeout(retryTimerRef.current)
    }
  }, [user, pushEvent])

  return (
    <NotificationContext.Provider value={{ toasts, dismissToast, lastEvent }}>
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const ctx = useContext(NotificationContext)
  if (!ctx) throw new Error('useNotifications must be used within NotificationProvider')
  return ctx
}
