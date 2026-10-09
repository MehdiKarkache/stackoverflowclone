import { useState } from 'react'
import Markdown from './Markdown'

interface Props {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  id?: string
}

/** Zone de texte Markdown avec onglet d'aperçu. */
export default function MarkdownEditor({ value, onChange, placeholder, id }: Props) {
  const [preview, setPreview] = useState(false)

  return (
    <div className="editor">
      <div className="editor-tabs" role="tablist">
        <button type="button" role="tab" aria-selected={!preview} className={preview ? '' : 'active'}
                onClick={() => setPreview(false)}>
          Écrire
        </button>
        <button type="button" role="tab" aria-selected={preview} className={preview ? 'active' : ''}
                onClick={() => setPreview(true)}>
          Aperçu
        </button>
      </div>
      {preview ? (
        <div className="preview">
          {value.trim() ? <Markdown>{value}</Markdown> : <span className="muted">Rien à afficher</span>}
        </div>
      ) : (
        <textarea id={id} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} />
      )}
    </div>
  )
}
