import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, getToken, setToken } from '../api/client'
import type { AuthResponse, UserSummary } from '../api/types'

interface AuthState {
  user: UserSummary | null
  loading: boolean
  login: (login: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => void
  /** Recharge l'utilisateur (ex. après un changement de réputation). */
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null)
  const [loading, setLoading] = useState(() => getToken() !== null)

  const refresh = useCallback(async () => {
    if (!getToken()) {
      setUser(null)
      return
    }
    try {
      setUser(await api<UserSummary>('/auth/me'))
    } catch {
      setUser(null)
    }
  }, [])

  useEffect(() => {
    refresh().finally(() => setLoading(false))
    const onLogout = () => setUser(null)
    window.addEventListener('auth:logout', onLogout)
    return () => window.removeEventListener('auth:logout', onLogout)
  }, [refresh])

  const value = useMemo<AuthState>(() => {
    const accept = (res: AuthResponse) => {
      setToken(res.token)
      setUser(res.user)
    }
    return {
      user,
      loading,
      refresh,
      login: async (login, password) => accept(await api<AuthResponse>('/auth/login', 'POST', { login, password })),
      register: async (username, email, password) =>
        accept(await api<AuthResponse>('/auth/register', 'POST', { username, email, password })),
      logout: () => {
        setToken(null)
        setUser(null)
      },
    }
  }, [user, loading, refresh])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth doit être utilisé dans <AuthProvider>')
  return ctx
}
