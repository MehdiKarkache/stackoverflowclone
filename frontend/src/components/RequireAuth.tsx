import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../auth/AuthContext'

/** Redirige vers /login (puis revient ici) si l'utilisateur n'est pas connecté. */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return null
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}
