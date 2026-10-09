import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { FormError, fieldError } from '../components/FormError'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ username: '', email: '', password: '' })
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await register(form.username, form.email, form.password)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  const field = (name: keyof typeof form, label: string, type = 'text', hint?: string) => (
    <label>
      {label}
      <input
        type={type}
        required
        value={form[name]}
        onChange={(e) => setForm({ ...form, [name]: e.target.value })}
      />
      {fieldError(error, name) ? (
        <span className="field-error">{fieldError(error, name)}</span>
      ) : (
        hint && <span className="hint">{hint}</span>
      )}
    </label>
  )

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={onSubmit}>
        <h1>Créer un compte</h1>
        <FormError error={error} />
        {field('username', "Nom d'utilisateur", 'text', "3 à 40 caractères : lettres, chiffres, '_', '.', '-'")}
        {field('email', 'Email', 'email')}
        {field('password', 'Mot de passe', 'password', '8 caractères minimum')}
        <button className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Création…' : "S'inscrire"}
        </button>
        <p className="muted center">
          Déjà inscrit ? <Link to="/login">Connectez-vous</Link>
        </p>
      </form>
    </div>
  )
}
