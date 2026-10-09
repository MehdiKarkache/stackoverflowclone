import { useState } from 'react'
import { useParams } from 'react-router'
import { api, ApiError, query } from '../api/client'
import type { Page, QuestionSummary, UserProfile } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { FormError } from '../components/FormError'
import Pagination from '../components/Pagination'
import QuestionItem from '../components/QuestionItem'
import { useFetch } from '../hooks/useFetch'
import { formatDate } from '../utils/time'
import NotFoundPage from './NotFoundPage'

const ROLE_LABELS: Record<string, string> = { ADMIN: 'admin', MODERATOR: 'modérateur' }

export default function UserPage() {
  const { id } = useParams()
  const { user: me } = useAuth()
  const [page, setPage] = useState(0)
  const { data: profile, error, setData } = useFetch<UserProfile>(`/users/${id}`)
  const { data: questions } = useFetch<Page<QuestionSummary>>(`/questions${query({ author: id, page })}`)

  const [editing, setEditing] = useState(false)
  const [bio, setBio] = useState('')

  if (error instanceof ApiError && error.status === 404) return <NotFoundPage />
  if (error) return <FormError error={error} />
  if (!profile) return <p className="muted">Chargement…</p>

  const isMe = me?.id === profile.id

  const saveBio = async () => {
    setData(await api<UserProfile>('/users/me', 'PUT', { bio }))
    setEditing(false)
  }

  return (
    <>
      <div className="profile">
        <span className="avatar large">{profile.username[0].toUpperCase()}</span>
        <div>
          <h1>
            {profile.username}
            {ROLE_LABELS[profile.role] && <span className="role-badge">{ROLE_LABELS[profile.role]}</span>}
          </h1>
          <div className="muted">Membre depuis le {formatDate(profile.createdAt)}</div>
          <div className="stats">
            <div>
              <b>{profile.reputation.toLocaleString('fr-FR')}</b>réputation
            </div>
            <div>
              <b>{profile.questionCount}</b>questions
            </div>
            <div>
              <b>{profile.answerCount}</b>réponses
            </div>
          </div>
        </div>
      </div>

      <h2>À propos</h2>
      {editing ? (
        <div className="form" style={{ gap: '0.5rem' }}>
          <textarea value={bio} maxLength={2000} onChange={(e) => setBio(e.target.value)} style={{ fontFamily: 'inherit' }} />
          <div className="post-actions">
            <button className="btn btn-primary" onClick={saveBio}>
              Enregistrer
            </button>
            <button className="btn btn-ghost" onClick={() => setEditing(false)}>
              Annuler
            </button>
          </div>
        </div>
      ) : (
        <p>
          {profile.bio || <span className="muted">Aucune description pour le moment.</span>}{' '}
          {isMe && (
            <button
              className="link-btn"
              onClick={() => {
                setBio(profile.bio ?? '')
                setEditing(true)
              }}
            >
              Modifier
            </button>
          )}
        </p>
      )}

      <h2>Questions ({questions?.totalElements ?? 0})</h2>
      <div className="question-list">
        {questions?.content.map((q) => <QuestionItem key={q.id} q={q} />)}
      </div>
      {questions?.content.length === 0 && <p className="muted">Aucune question posée.</p>}
      {questions && <Pagination page={questions.page} totalPages={questions.totalPages} onChange={setPage} />}
    </>
  )
}
