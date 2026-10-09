import { useState, type FormEvent } from 'react'
import { Link, NavLink, Outlet, useNavigate, useSearchParams } from 'react-router'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [search, setSearch] = useState(params.get('q') ?? '')

  const onSearch = (e: FormEvent) => {
    e.preventDefault()
    const q = search.trim()
    navigate(q ? `/?q=${encodeURIComponent(q)}` : '/')
  }

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="logo">
            <span className="logo-mark" aria-hidden>
              <span />
              <span />
              <span />
            </span>
            stack<b>clone</b>
          </Link>
          <form className="search" onSubmit={onSearch} role="search">
            <input
              type="search"
              placeholder="Rechercher…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Rechercher des questions"
            />
          </form>
          <nav className="topbar-actions">
            {user ? (
              <>
                <Link to={`/users/${user.id}`} className="me">
                  <span className="avatar small">{user.username[0].toUpperCase()}</span>
                  <span className="me-name">{user.username}</span>
                  <b className="rep">{user.reputation}</b>
                </Link>
                <button className="btn btn-ghost" onClick={logout}>
                  Déconnexion
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-outline">
                  Connexion
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Inscription
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <div className="container">
        <aside className="sidebar">
          <nav>
            <NavLink to="/" end>
              Questions
            </NavLink>
            <NavLink to="/tags">Tags</NavLink>
            <NavLink to="/users">Utilisateurs</NavLink>
          </nav>
        </aside>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </>
  )
}
