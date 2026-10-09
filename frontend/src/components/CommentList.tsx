import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { api } from '../api/client'
import type { Comment } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { timeAgo } from '../utils/time'

interface Props {
  postId: number
  initial: Comment[]
}

export default function CommentList({ postId, initial }: Props) {
  const { user } = useAuth()
  const [comments, setComments] = useState(initial)
  const [open, setOpen] = useState(false)
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | null>(null)

  const canModerate = user?.role === 'ADMIN' || user?.role === 'MODERATOR'

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    try {
      const created = await api<Comment>(`/posts/${postId}/comments`, 'POST', { body })
      setComments([...comments, created])
      setBody('')
      setOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur')
    }
  }

  const remove = async (id: number) => {
    if (!confirm('Supprimer ce commentaire ?')) return
    await api(`/comments/${id}`, 'DELETE')
    setComments(comments.filter((c) => c.id !== id))
  }

  return (
    <div className="comments">
      {comments.map((c) => (
        <div key={c.id} className="comment">
          {c.body} – <Link to={`/users/${c.author.id}`}>{c.author.username}</Link>{' '}
          <span className="meta">{timeAgo(c.createdAt)}</span>
          {(user?.id === c.author.id || canModerate) && (
            <>
              {' '}
              <button className="link-btn" onClick={() => remove(c.id)}>
                supprimer
              </button>
            </>
          )}
        </div>
      ))}
      {user &&
        (open ? (
          <form className="comment-form" onSubmit={submit}>
            <div style={{ flex: 1 }}>
              <textarea
                autoFocus
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Demandez des précisions ou suggérez une amélioration (5 à 600 caractères)"
                maxLength={600}
              />
              {error && <span className="field-error">{error}</span>}
            </div>
            <button className="btn btn-primary" disabled={body.trim().length < 5}>
              Commenter
            </button>
          </form>
        ) : (
          <button className="link-btn" style={{ marginTop: '0.5rem' }} onClick={() => setOpen(true)}>
            Ajouter un commentaire
          </button>
        ))}
    </div>
  )
}
