import { createContext, useContext, useState, useCallback } from 'react'
import apiClient from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('inter_a2z_user')
    return raw ? JSON.parse(raw) : null
  })

  const login = useCallback(async (email, password) => {
    const { data } = await apiClient.post('/api/auth/login', { email, password })
    persist(data)
    return data
  }, [])

  const signup = useCallback(async (email, password, name, department) => {
    const { data } = await apiClient.post('/api/auth/signup', { email, password, name, department })
    persist(data)
    return data
  }, [])

  const persist = (data) => {
    localStorage.setItem('inter_a2z_token', data.token)
    localStorage.setItem('inter_a2z_refresh_token', data.refreshToken)
    const userInfo = { userId: data.userId, name: data.name, email: data.email, role: data.role }
    localStorage.setItem('inter_a2z_user', JSON.stringify(userInfo))
    setUser(userInfo)
  }

  const logout = useCallback(() => {
    localStorage.removeItem('inter_a2z_token')
    localStorage.removeItem('inter_a2z_refresh_token')
    localStorage.removeItem('inter_a2z_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
