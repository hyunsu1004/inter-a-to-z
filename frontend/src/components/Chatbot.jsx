import { useState, useRef, useEffect } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import apiClient from '../api/client'

export default function Chatbot() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: 'bot', text: '안녕하세요! INTER A to Z 챗봇이에요. 핵심가치, 미션, 포인트에 대해 뭐든 물어보세요 :)' },
  ])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const listRef = useRef(null)

  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight
    }
  }, [messages, open])

  const send = async () => {
    const text = input.trim()
    if (!text || sending) return
    setMessages((prev) => [...prev, { role: 'user', text }])
    setInput('')
    setSending(true)
    try {
      const { data } = await apiClient.post('/api/chatbot/ask', { message: text })
      setMessages((prev) => [...prev, { role: 'bot', text: data.answer }])
    } catch {
      setMessages((prev) => [...prev, { role: 'bot', text: '잠시 후 다시 시도해주세요.' }])
    } finally {
      setSending(false)
    }
  }

  return (
    <>
      <button
        onClick={() => setOpen((o) => !o)}
        type="button"
        aria-label="챗봇 열기"
        style={{
          position: 'absolute',
          right: 16,
          bottom: 16,
          width: 52,
          height: 52,
          borderRadius: '50%',
          background: 'var(--brand-300)',
          border: 'none',
          boxShadow: '0 4px 12px rgba(242,121,12,0.35)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 40,
        }}
      >
        <i className={`ti ${open ? 'ti-x' : 'ti-message-circle-2'}`} style={{ fontSize: 22, color: '#FFF7EC' }} />
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 16 }}
            style={{
              position: 'absolute',
              right: 12,
              bottom: 76,
              left: 12,
              maxWidth: 420,
              margin: '0 auto',
              background: 'var(--surface-1)',
              border: '1px solid var(--border)',
              borderRadius: 18,
              boxShadow: '0 10px 30px rgba(0,0,0,0.12)',
              display: 'flex',
              flexDirection: 'column',
              height: 360,
              zIndex: 40,
            }}
          >
            <div style={{ padding: '12px 14px', borderBottom: '1px solid var(--border)', fontSize: 13, fontWeight: 700 }}>
              INTER A to Z 챗봇
            </div>
            <div ref={listRef} style={{ flex: 1, overflowY: 'auto', padding: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
              {messages.map((m, i) => (
                <div
                  key={i}
                  style={{
                    alignSelf: m.role === 'user' ? 'flex-end' : 'flex-start',
                    background: m.role === 'user' ? 'var(--brand-300)' : 'var(--surface-2)',
                    color: m.role === 'user' ? '#FFF7EC' : 'var(--text-primary)',
                    padding: '8px 12px',
                    borderRadius: 12,
                    fontSize: 13,
                    maxWidth: '80%',
                  }}
                >
                  {m.text}
                </div>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 8, padding: 10, borderTop: '1px solid var(--border)' }}>
              <input
                className="input"
                style={{ height: 38, flex: 1 }}
                placeholder="메시지를 입력하세요"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && send()}
              />
              <button
                className="btn-primary"
                style={{ width: 56, height: 38 }}
                onClick={send}
                disabled={sending}
                type="button"
              >
                <i className="ti ti-arrow-right" style={{ fontSize: 16 }} />
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}
