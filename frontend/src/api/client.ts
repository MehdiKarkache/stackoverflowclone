const TOKEN_KEY = 'stackclone.token'

/** Erreur renvoyée par l'API, au format { status, message, errors } du backend. */
export class ApiError extends Error {
  readonly status: number
  readonly errors: Record<string, string>

  constructor(status: number, message: string, errors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token: string | null) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    // stockage indisponible (navigation privée) : la session ne survivra pas au rechargement
  }
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

export async function api<T>(path: string, method: Method = 'GET', body?: unknown): Promise<T> {
  const token = getToken()
  const res = await send(path, method, body, token)

  if (res.status === 401 && token) {
    // Token expiré ou invalide : on déconnecte, et on réessaie les lectures en anonyme
    setToken(null)
    window.dispatchEvent(new Event('auth:logout'))
    if (method === 'GET') return api<T>(path, method)
  }

  if (!res.ok) {
    const data = await res.json().catch(() => null)
    const message = data?.message ?? (res.status === 401 ? 'Vous devez être connecté' : `Erreur ${res.status}`)
    throw new ApiError(res.status, message, data?.errors ?? {})
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

function send(path: string, method: Method, body: unknown, token: string | null) {
  const headers: Record<string, string> = {}
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  return fetch(`/api${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })
}

/** Construit une query string en ignorant les valeurs vides. */
export function query(params: Record<string, string | number | undefined | null>): string {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') search.set(key, String(value))
  }
  const s = search.toString()
  return s ? `?${s}` : ''
}
