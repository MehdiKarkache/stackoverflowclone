import { ApiError } from '../api/client'

/** Message d'erreur global d'un formulaire. */
export function FormError({ error }: { error: unknown }) {
  if (!error) return null
  const message = error instanceof Error ? error.message : 'Une erreur est survenue'
  return (
    <div className="alert alert-error" role="alert">
      {message}
    </div>
  )
}

/** Erreur de validation d'un champ précis (renvoyée par le backend). */
export function fieldError(error: unknown, field: string): string | undefined {
  return error instanceof ApiError ? error.errors[field] : undefined
}
