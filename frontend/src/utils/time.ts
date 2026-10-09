const rtf = new Intl.RelativeTimeFormat('fr', { numeric: 'auto' })

const UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
  ['year', 365 * 24 * 3600],
  ['month', 30 * 24 * 3600],
  ['day', 24 * 3600],
  ['hour', 3600],
  ['minute', 60],
]

/** "il y a 5 minutes", "hier", "il y a 2 mois"… */
export function timeAgo(iso: string): string {
  const seconds = (new Date(iso).getTime() - Date.now()) / 1000
  for (const [unit, size] of UNITS) {
    if (Math.abs(seconds) >= size) return rtf.format(Math.round(seconds / size), unit)
  }
  return "à l'instant"
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}

/** 1234 -> "1,2k" */
export function compact(n: number): string {
  return new Intl.NumberFormat('fr', { notation: 'compact', maximumFractionDigits: 1 }).format(n)
}
