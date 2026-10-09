import { useState } from 'react'
import { api } from '../api/client'
import type { VoteResult } from '../api/types'
import { useAuth } from '../auth/AuthContext'

interface Props {
  postId: number
  score: number
  myVote: number
  isOwn: boolean
  /** Bouton "accepter" (uniquement pour les réponses). */
  accept?: { accepted: boolean; canAccept: boolean; onToggle: () => void }
}

export default function VoteButtons({ postId, score: initialScore, myVote: initialVote, isOwn, accept }: Props) {
  const { user, refresh } = useAuth()
  const [score, setScore] = useState(initialScore)
  const [myVote, setMyVote] = useState(initialVote)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const vote = async (value: 1 | -1) => {
    if (!user) {
      setError('Connectez-vous pour voter')
      return
    }
    // Recliquer sur le même vote l'annule
    const next = myVote === value ? 0 : value
    setBusy(true)
    setError(null)
    try {
      const res = await api<VoteResult>(`/posts/${postId}/vote`, 'POST', { value: next })
      setScore(res.score)
      setMyVote(res.myVote)
      refresh() // la réputation du votant peut changer (downvote d'une réponse)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setBusy(false)
    }
  }

  const disabled = busy || isOwn
  const ownTitle = isOwn ? 'Vous ne pouvez pas voter pour votre propre post' : undefined

  return (
    <div className="vote">
      <button
        className={myVote === 1 ? 'on' : ''}
        disabled={disabled}
        onClick={() => vote(1)}
        title={ownTitle ?? 'Ce post est utile et clair'}
        aria-label="Voter pour"
        aria-pressed={myVote === 1}
      >
        <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden>
          <path d="M1 12h16L9 4l-8 8Z" fill="currentColor" />
        </svg>
      </button>
      <span className="score" aria-label="Score">
        {score}
      </span>
      <button
        className={myVote === -1 ? 'on' : ''}
        disabled={disabled}
        onClick={() => vote(-1)}
        title={ownTitle ?? "Ce post n'est pas utile ou pas clair"}
        aria-label="Voter contre"
        aria-pressed={myVote === -1}
      >
        <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden>
          <path d="M1 6h16l-8 8-8-8Z" fill="currentColor" />
        </svg>
      </button>
      {accept && (accept.accepted || accept.canAccept) && (
        <button
          className={`accept ${accept.accepted ? 'on' : ''}`}
          disabled={!accept.canAccept}
          onClick={accept.onToggle}
          title={accept.accepted ? 'Réponse acceptée' : 'Accepter cette réponse'}
          aria-label={accept.accepted ? 'Réponse acceptée' : 'Accepter cette réponse'}
          aria-pressed={accept.accepted}
        >
          <svg width="32" height="32" viewBox="0 0 36 36" aria-hidden>
            <path d="m6 14 8 8L30 6v8L14 30l-8-8v-8Z" fill="currentColor" />
          </svg>
        </button>
      )}
      {error && <p className="vote-error">{error}</p>}
    </div>
  )
}
