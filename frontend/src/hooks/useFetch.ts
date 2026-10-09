import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'

interface FetchState<T> {
  data: T | null
  error: unknown
  loading: boolean
  reload: () => void
  setData: (data: T) => void
}

/** GET d'un chemin de l'API, rechargé quand le chemin change. */
export function useFetch<T>(path: string | null): FetchState<T> {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<unknown>(null)
  const [loading, setLoading] = useState(path !== null)
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (path === null) return
    let cancelled = false
    setLoading(true)
    setError(null)
    api<T>(path)
      .then((d) => !cancelled && setData(d))
      .catch((e) => !cancelled && setError(e))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [path, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])
  return { data, error, loading, reload, setData }
}
