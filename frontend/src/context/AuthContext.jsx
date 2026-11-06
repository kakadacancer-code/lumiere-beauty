import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [checking, setChecking] = useState(Boolean(localStorage.getItem('lumiere-token')))

  useEffect(() => {
    let alive = true
    if (!localStorage.getItem('lumiere-token')) return
    authApi.me()
      .then(account => { if (alive) setUser(account) })
      .catch(() => {
        if (!alive) return
        localStorage.removeItem('lumiere-token')
        setUser(null)
      })
      .finally(() => { if (alive) setChecking(false) })
    return () => { alive = false }
  }, [])

  function acceptSession(session) {
    localStorage.setItem('lumiere-token', session.token)
    setUser(session.user)
  }
  async function login(credentials) {
    const session = await authApi.login(credentials)
    acceptSession(session)
    return session.user
  }
  async function register(data) {
    const session = await authApi.register(data)
    acceptSession(session)
    return session.user
  }
  async function logout() {
    try { await authApi.logout() } catch (error) { console.warn('Could not revoke session:', error) }
    localStorage.removeItem('lumiere-token')
    setUser(null)
  }

  const value = useMemo(() => ({ user, checking, login, register, logout, isAdmin: user?.role === 'Admin' }), [user, checking])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export const useAuth = () => useContext(AuthContext)
