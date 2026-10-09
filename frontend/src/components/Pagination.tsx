interface Props {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

/** Pagination compacte : première, dernière, et deux pages autour de la courante. */
export default function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null

  const pages: (number | '…')[] = []
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 2) pages.push(i)
    else if (pages[pages.length - 1] !== '…') pages.push('…')
  }

  return (
    <nav className="pagination" aria-label="Pagination">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}>
        Préc.
      </button>
      {pages.map((p, i) =>
        p === '…' ? (
          <span key={`gap-${i}`} className="muted">
            …
          </span>
        ) : (
          <button key={p} className={p === page ? 'active' : ''} disabled={p === page} onClick={() => onChange(p)}>
            {p + 1}
          </button>
        ),
      )}
      <button disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Suiv.
      </button>
    </nav>
  )
}
