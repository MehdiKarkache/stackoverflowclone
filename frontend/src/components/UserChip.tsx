import { Link } from 'react-router'
import type { UserSummary } from '../api/types'

export default function UserChip({ user }: { user: UserSummary }) {
  return (
    <span className="user-chip">
      <span className="avatar small">{user.username[0].toUpperCase()}</span>
      <Link to={`/users/${user.id}`}>{user.username}</Link>
      <b>{user.reputation}</b>
    </span>
  )
}
