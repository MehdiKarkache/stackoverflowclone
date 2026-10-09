import { Link } from 'react-router'
import type { QuestionSummary } from '../api/types'
import { compact, timeAgo } from '../utils/time'
import UserChip from './UserChip'

export default function QuestionItem({ q }: { q: QuestionSummary }) {
  const answersClass = ['answers', q.answerCount > 0 && 'has-answers', q.answered && 'accepted']
    .filter(Boolean)
    .join(' ')

  return (
    <article className="question-item">
      <div className="question-stats">
        <span className="votes">
          {q.score} vote{Math.abs(q.score) > 1 ? 's' : ''}
        </span>
        <span className={answersClass}>
          {q.answered && '✓ '}
          {q.answerCount} réponse{q.answerCount > 1 ? 's' : ''}
        </span>
        <span>{compact(q.viewCount)} vues</span>
      </div>
      <div className="question-body">
        <h3 className="question-title">
          <Link to={`/questions/${q.id}`}>{q.title}</Link>
        </h3>
        <p className="question-excerpt">{q.excerpt}</p>
        <div className="question-meta">
          <div className="tags">
            {q.tags.map((t) => (
              <Link key={t} to={`/?tag=${encodeURIComponent(t)}`} className="tag">
                {t}
              </Link>
            ))}
          </div>
          <span className="user-chip">
            <UserChip user={q.author} /> a demandé {timeAgo(q.createdAt)}
          </span>
        </div>
      </div>
    </article>
  )
}
