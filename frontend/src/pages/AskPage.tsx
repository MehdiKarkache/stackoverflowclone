import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router'
import { api } from '../api/client'
import type { QuestionDetail } from '../api/types'
import { FormError, fieldError } from '../components/FormError'
import MarkdownEditor from '../components/MarkdownEditor'
import TagInput from '../components/TagInput'

/** Création d'une question, ou modification si l'URL contient un id. */
export default function AskPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const editing = id !== undefined

  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [tags, setTags] = useState<string[]>([])
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!editing) return
    api<QuestionDetail>(`/questions/${id}`)
      .then((q) => {
        setTitle(q.title)
        setBody(q.body)
        setTags(q.tags)
      })
      .catch(setError)
  }, [editing, id])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const payload = { title, body, tags }
      const q = editing
        ? await api<QuestionDetail>(`/questions/${id}`, 'PUT', payload)
        : await api<QuestionDetail>('/questions', 'POST', payload)
      navigate(`/questions/${q.id}`)
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <h1>{editing ? 'Modifier la question' : 'Poser une question publique'}</h1>
      <form className="form" onSubmit={submit}>
        <FormError error={error} />

        <div className="card">
          <label htmlFor="title">Titre</label>
          <span className="hint">Soyez précis et imaginez que vous posez la question à quelqu'un (15 à 150 caractères).</span>
          <input
            id="title"
            value={title}
            maxLength={150}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="ex. Comment trier une Map par valeur en Java ?"
          />
          {fieldError(error, 'title') && <span className="field-error">{fieldError(error, 'title')}</span>}
        </div>

        <div className="card">
          <label htmlFor="body">Détails du problème</label>
          <span className="hint">
            Expliquez ce que vous avez essayé et ce que vous attendiez (30 caractères minimum). Markdown supporté.
          </span>
          <MarkdownEditor id="body" value={body} onChange={setBody} />
          {fieldError(error, 'body') && <span className="field-error">{fieldError(error, 'body')}</span>}
        </div>

        <div className="card">
          <label htmlFor="tags">Tags</label>
          <span className="hint">Jusqu'à 5 tags pour décrire le sujet. Validez chaque tag avec Entrée ou espace.</span>
          <TagInput value={tags} onChange={setTags} />
          {fieldError(error, 'tags') && <span className="field-error">{fieldError(error, 'tags')}</span>}
        </div>

        <div>
          <button className="btn btn-primary" disabled={submitting}>
            {editing ? 'Enregistrer les modifications' : 'Publier la question'}
          </button>
        </div>
      </form>
    </>
  )
}
