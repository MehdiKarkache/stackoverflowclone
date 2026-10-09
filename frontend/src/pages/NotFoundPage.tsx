import { Link } from 'react-router'

export default function NotFoundPage() {
  return (
    <div className="empty">
      <h1>Page introuvable</h1>
      <p className="muted">Cette page n'existe pas ou a été supprimée.</p>
      <Link to="/" className="btn btn-primary">
        Retour aux questions
      </Link>
    </div>
  )
}
