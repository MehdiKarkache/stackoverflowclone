import { useState } from 'react'
import { Link } from 'react-router'
import { query } from '../api/client'
import type { Page, TagUsage } from '../api/types'
import { FormError } from '../components/FormError'
import Pagination from '../components/Pagination'
import { useDebounced } from '../hooks/useDebounced'
import { useFetch } from '../hooks/useFetch'

export default function TagsPage() {
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(0)
  const q = useDebounced(filter.trim())
  const { data, error } = useFetch<Page<TagUsage>>(`/tags${query({ q, page })}`)

  return (
    <>
      <h1>Tags</h1>
      <p className="muted">Un tag est un mot-clé qui classe une question avec d'autres questions similaires.</p>
      <input
        className="filter"
        type="search"
        placeholder="Filtrer par nom"
        value={filter}
        onChange={(e) => {
          setFilter(e.target.value)
          setPage(0)
        }}
      />
      <FormError error={error} />
      <div className="grid">
        {data?.content.map((t) => (
          <div key={t.id} className="card tag-card">
            <Link to={`/?tag=${encodeURIComponent(t.name)}`} className="tag">
              {t.name}
            </Link>
            {t.description && <p>{t.description}</p>}
            <p className="muted">
              {t.questionCount} question{t.questionCount > 1 ? 's' : ''}
            </p>
          </div>
        ))}
      </div>
      {data?.content.length === 0 && <p className="muted">Aucun tag trouvé.</p>}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  )
}
