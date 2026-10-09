import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

/**
 * Rendu Markdown sécurisé : react-markdown n'interprète pas le HTML brut,
 * donc pas de risque d'injection de scripts (XSS).
 */
export default function Markdown({ children }: { children: string }) {
  return (
    <div className="markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          a: ({ node: _node, ...props }) => <a {...props} target="_blank" rel="noopener noreferrer nofollow" />,
        }}
      >
        {children}
      </ReactMarkdown>
    </div>
  )
}
