import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api, ApiError } from '../api/client'
import type { Answer, QuestionDetail } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import CommentList from '../components/CommentList'
import { FormError } from '../components/FormError'
import Markdown from '../components/Markdown'
import MarkdownEditor from '../components/MarkdownEditor'
import UserChip from '../components/UserChip'
import VoteButtons from '../components/VoteButtons'
import { useFetch } from '../hooks/useFetch'
import { compact, timeAgo } from '../utils/time'
import NotFoundPage from './NotFoundPage'

export default function QuestionPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, refresh } = useAuth()
  const { data: question, error, setData } = useFetch<QuestionDetail>(`/questions/${id}`)

  if (error instanceof ApiError && error.status === 404) return <NotFoundPage />
  if (error) return <FormError error={error} />
  if (!question) return <p className="muted">Chargement…</p>

  const isModerator = user?.role === 'ADMIN' || user?.role === 'MODERATOR'
  const isAsker = user?.id === question.author.id
  const canEdit = (authorId: number) => user?.id === authorId || isModerator

  const deleteQuestion = async () => {
    if (!confirm('Supprimer définitivement cette question et ses réponses ?')) return
    await api(`/questions/${question.id}`, 'DELETE')
    navigate('/')
  }

  const toggleAccept = async (answer: Answer) => {
    await api(`/answers/${answer.id}/accept`, 'POST')
    const acceptedAnswerId = question.acceptedAnswerId === answer.id ? null : answer.id
    setData({
      ...question,
      acceptedAnswerId,
      answers: question.answers.map((a) => ({ ...a, accepted: a.id === acceptedAnswerId })),
    })
    refresh()
  }

  const replaceAnswer = (updated: Answer) =>
    setData({ ...question, answers: question.answers.map((a) => (a.id === updated.id ? updated : a)) })

  const removeAnswer = (answerId: number) =>
    setData({
      ...question,
      acceptedAnswerId: question.acceptedAnswerId === answerId ? null : question.acceptedAnswerId,
      answers: question.answers.filter((a) => a.id !== answerId),
    })

  return (
    <>
      <div className="page-header">
        <h1>{question.title}</h1>
        <Link to="/ask" className="btn btn-primary">
          Poser une question
        </Link>
      </div>
      <div className="question-info">
        <span>
          Posée <b>{timeAgo(question.createdAt)}</b>
        </span>
        {question.updatedAt && (
          <span>
            Modifiée <b>{timeAgo(question.updatedAt)}</b>
          </span>
        )}
        <span>
          Vue <b>{compact(question.viewCount)} fois</b>
        </span>
      </div>

      <div className="post">
        <VoteButtons postId={question.id} score={question.score} myVote={question.myVote} isOwn={isAsker} />
        <div className="post-main">
          <Markdown>{question.body}</Markdown>
          <div className="tags" style={{ marginTop: '1rem' }}>
            {question.tags.map((t) => (
              <Link key={t} to={`/?tag=${encodeURIComponent(t)}`} className="tag">
                {t}
              </Link>
            ))}
          </div>
          <div className="post-footer">
            <div className="post-actions">
              {canEdit(question.author.id) && (
                <>
                  <Link to={`/questions/${question.id}/edit`} className="link-btn">
                    Modifier
                  </Link>
                  <button className="link-btn" onClick={deleteQuestion}>
                    Supprimer
                  </button>
                </>
              )}
            </div>
            <div className="post-author">
              <span className="muted">a demandé {timeAgo(question.createdAt)}</span>
              <div>
                <UserChip user={question.author} />
              </div>
            </div>
          </div>
          <CommentList postId={question.id} initial={question.comments} />
        </div>
      </div>

      <div className="answers-header">
        <h2>
          {question.answers.length} réponse{question.answers.length > 1 ? 's' : ''}
        </h2>
      </div>

      {question.answers.map((a) => (
        <AnswerView
          key={a.id}
          answer={a}
          isOwn={user?.id === a.author.id}
          canEdit={canEdit(a.author.id)}
          canAccept={isAsker}
          onAcceptToggle={() => toggleAccept(a)}
          onUpdated={replaceAnswer}
          onDeleted={() => removeAnswer(a.id)}
        />
      ))}

      <AnswerForm
        questionId={question.id}
        closed={question.status !== 'OPEN'}
        onCreated={(a) => setData({ ...question, answers: [...question.answers, a] })}
      />
    </>
  )
}

interface AnswerViewProps {
  answer: Answer
  isOwn: boolean
  canEdit: boolean
  canAccept: boolean
  onAcceptToggle: () => void
  onUpdated: (a: Answer) => void
  onDeleted: () => void
}

function AnswerView({ answer, isOwn, canEdit, canAccept, onAcceptToggle, onUpdated, onDeleted }: AnswerViewProps) {
  const [editing, setEditing] = useState(false)
  const [body, setBody] = useState(answer.body)
  const [error, setError] = useState<unknown>(null)

  const save = async () => {
    setError(null)
    try {
      onUpdated(await api<Answer>(`/answers/${answer.id}`, 'PUT', { body }))
      setEditing(false)
    } catch (e) {
      setError(e)
    }
  }

  const remove = async () => {
    if (!confirm('Supprimer cette réponse ?')) return
    await api(`/answers/${answer.id}`, 'DELETE')
    onDeleted()
  }

  return (
    <div className="post" id={`answer-${answer.id}`}>
      <VoteButtons
        postId={answer.id}
        score={answer.score}
        myVote={answer.myVote}
        isOwn={isOwn}
        accept={{ accepted: answer.accepted, canAccept, onToggle: onAcceptToggle }}
      />
      <div className="post-main">
        {editing ? (
          <div className="form" style={{ gap: '0.75rem' }}>
            <FormError error={error} />
            <MarkdownEditor value={body} onChange={setBody} />
            <div className="post-actions">
              <button className="btn btn-primary" onClick={save}>
                Enregistrer
              </button>
              <button className="btn btn-ghost" onClick={() => setEditing(false)}>
                Annuler
              </button>
            </div>
          </div>
        ) : (
          <Markdown>{answer.body}</Markdown>
        )}
        <div className="post-footer">
          <div className="post-actions">
            {canEdit && !editing && (
              <>
                <button className="link-btn" onClick={() => setEditing(true)}>
                  Modifier
                </button>
                <button className="link-btn" onClick={remove}>
                  Supprimer
                </button>
              </>
            )}
          </div>
          <div className="post-author answer-author">
            <span className="muted">
              a répondu {timeAgo(answer.createdAt)}
              {answer.updatedAt && ` · modifiée ${timeAgo(answer.updatedAt)}`}
            </span>
            <div>
              <UserChip user={answer.author} />
            </div>
          </div>
        </div>
        <CommentList postId={answer.id} initial={answer.comments} />
      </div>
    </div>
  )
}

function AnswerForm({ questionId, closed, onCreated }: { questionId: number; closed: boolean; onCreated: (a: Answer) => void }) {
  const { user } = useAuth()
  const [body, setBody] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  if (closed) return <p className="muted answer-form">Cette question est fermée aux nouvelles réponses.</p>
  if (!user) {
    return (
      <p className="answer-form">
        <Link to="/login" state={{ from: `/questions/${questionId}` }}>
          Connectez-vous
        </Link>{' '}
        pour répondre à cette question.
      </p>
    )
  }

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      onCreated(await api<Answer>(`/questions/${questionId}/answers`, 'POST', { body }))
      setBody('')
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="form answer-form" onSubmit={submit}>
      <h2 style={{ margin: 0 }}>Votre réponse</h2>
      <FormError error={error} />
      <MarkdownEditor value={body} onChange={setBody} placeholder="Markdown supporté : **gras**, `code`, ```blocs de code```…" />
      <div>
        <button className="btn btn-primary" disabled={submitting || body.trim().length < 20}>
          Publier votre réponse
        </button>
      </div>
    </form>
  )
}
