import { useState } from 'react'
import { Link } from 'react-router'
import { query } from '../api/client'
import type { Page, UserSummary } from '../api/types'
import { FormError } from '../components/FormError'
import Pagination from '../components/Pagination'
import { useDebounced } from '../hooks/useDebounced'
import { useFetch } from '../hooks/useFetch'

export default function UsersPage() {
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(0)
  const q = useDebounced(filter.trim())
  const { data, error } = useFetch<Page<UserSummary>>(`/users${query({ q, page })}`)

  return (
    <>
      <h1>Utilisateurs</h1>
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
        {data?.content.map((u) => (
          <div key={u.id} className="card user-card">
            <span className="avatar">{u.username[0].toUpperCase()}</span>
            <div>
              <Link to={`/users/${u.id}`}>{u.username}</Link>
              <div className="muted">{u.reputation.toLocaleString('fr-FR')} réputation</div>
            </div>
          </div>
        ))}
      </div>
      {data?.content.length === 0 && <p className="muted">Aucun utilisateur trouvé.</p>}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  )
}
