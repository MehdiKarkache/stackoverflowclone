import { Link, useSearchParams } from 'react-router'
import { query } from '../api/client'
import type { Page, QuestionSummary } from '../api/types'
import { FormError } from '../components/FormError'
import Pagination from '../components/Pagination'
import QuestionItem from '../components/QuestionItem'
import { useFetch } from '../hooks/useFetch'

const SORTS = [
  { value: 'newest', label: 'Récentes' },
  { value: 'votes', label: 'Mieux notées' },
  { value: 'unanswered', label: 'Sans réponse' },
  { value: 'views', label: 'Plus vues' },
]

export default function HomePage() {
  const [params, setParams] = useSearchParams()
  const sort = params.get('sort') ?? 'newest'
  const tag = params.get('tag') ?? ''
  const q = params.get('q') ?? ''
  const page = Number(params.get('page') ?? 0)

  const { data, error, loading } = useFetch<Page<QuestionSummary>>(
    `/questions${query({ sort, tag, q, page })}`,
  )

  const update = (changes: Record<string, string | number | null>) => {
    const next = new URLSearchParams(params)
    for (const [k, v] of Object.entries(changes)) {
      if (v === null || v === '' || v === 0) next.delete(k)
      else next.set(k, String(v))
    }
    if (!('page' in changes)) next.delete('page')
    setParams(next)
    window.scrollTo(0, 0)
  }

  const title = q ? `Résultats pour « ${q} »` : tag ? `Questions taguées [${tag}]` : 'Toutes les questions'

  return (
    <>
      <div className="page-header">
        <h1>{title}</h1>
        <Link to="/ask" className="btn btn-primary">
          Poser une question
        </Link>
      </div>

      <div className="toolbar">
        <span className="muted">
          {data ? `${data.totalElements.toLocaleString('fr-FR')} question${data.totalElements > 1 ? 's' : ''}` : ' '}
          {(q || tag) && (
            <>
              {' · '}
              <Link to="/">effacer les filtres</Link>
            </>
          )}
        </span>
        <div className="segmented" role="group" aria-label="Trier les questions">
          {SORTS.map((s) => (
            <button key={s.value} className={sort === s.value ? 'active' : ''} onClick={() => update({ sort: s.value === 'newest' ? null : s.value })}>
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <FormError error={error} />

      <div className="question-list" aria-busy={loading}>
        {data?.content.map((question) => <QuestionItem key={question.id} q={question} />)}
      </div>
      {data && data.content.length === 0 && (
        <div className="empty">
          <p className="muted">Aucune question trouvée.</p>
          <Link to="/ask" className="btn btn-primary">
            Poser la première question
          </Link>
        </div>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={(p) => update({ page: p })} />}
    </>
  )
}
