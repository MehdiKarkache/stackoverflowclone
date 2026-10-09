import { useState, type KeyboardEvent } from 'react'

const MAX_TAGS = 5
const TAG_PATTERN = /^[a-z0-9+#.-]{1,35}$/

interface Props {
  value: string[]
  onChange: (tags: string[]) => void
}

/** Saisie de tags : Entrée, virgule ou espace pour valider un tag. */
export default function TagInput({ value, onChange }: Props) {
  const [draft, setDraft] = useState('')

  const add = (raw: string) => {
    const tag = raw.trim().toLowerCase()
    if (!tag || !TAG_PATTERN.test(tag) || value.includes(tag) || value.length >= MAX_TAGS) return
    onChange([...value, tag])
    setDraft('')
  }

  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',' || e.key === ' ') {
      e.preventDefault()
      add(draft)
    } else if (e.key === 'Backspace' && !draft && value.length) {
      onChange(value.slice(0, -1))
    }
  }

  return (
    <div className="tag-input">
      {value.map((t) => (
        <span key={t} className="tag">
          {t}
          <button type="button" aria-label={`Retirer ${t}`} onClick={() => onChange(value.filter((x) => x !== t))}>
            ✕
          </button>
        </span>
      ))}
      {value.length < MAX_TAGS && (
        <input
          id="tags"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={onKeyDown}
          onBlur={() => add(draft)}
          placeholder={value.length ? '' : 'ex. java spring-boot react'}
        />
      )}
    </div>
  )
}
