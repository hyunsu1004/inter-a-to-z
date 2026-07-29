import { useEffect, useState } from 'react'
import apiClient from '../api/client'

export default function MissionCommentThread({ userMissionId, isAdmin = false, lastEvent }) {
  const [comments, setComments] = useState([])
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(true)
  const [posting, setPosting] = useState(false)

  const basePath = isAdmin
    ? `/api/admin/missions/submissions/${userMissionId}/comments`
    : `/api/missions/submissions/${userMissionId}/comments`

  const load = () => {
    apiClient.get(basePath).then((res) => setComments(res.data)).finally(() => setLoading(false))
  }

  useEffect(() => {
    if (userMissionId) load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userMissionId])

  // 실시간 알림으로 이 스레드에 새 댓글이 달렸다는 이벤트를 받으면 자동으로 다시 불러온다.
  useEffect(() => {
    if (lastEvent?.type === 'NEW_COMMENT' && lastEvent.userMissionId === userMissionId) {
      load()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastEvent])

  const handleSend = async () => {
    if (!text.trim()) return
    setPosting(true)
    try {
      await apiClient.post(basePath, { content: text.trim() })
      setText('')
      load()
    } finally {
      setPosting(false)
    }
  }

  if (!userMissionId) return null

  return (
    <div style={{ marginTop: 10, borderTop: '1px solid var(--border)', paddingTop: 10 }}>
      <p style={{ margin: '0 0 8px', fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)' }}>
        <i className="ti ti-messages" style={{ verticalAlign: -2, marginRight: 4 }} />
        코멘트{comments.length > 0 ? ` (${comments.length})` : ''}
      </p>

      {loading ? (
        <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>불러오는 중...</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 8 }}>
          {comments.map((c) => (
            <div
              key={c.id}
              style={{
                alignSelf: c.authorRole === 'ADMIN' ? 'flex-start' : 'flex-end',
                background: c.authorRole === 'ADMIN' ? 'var(--surface-2)' : 'var(--brand-50)',
                borderRadius: 10,
                padding: '8px 10px',
                maxWidth: '85%',
              }}
            >
              <p style={{ margin: 0, fontSize: 11, fontWeight: 700, color: 'var(--text-secondary)' }}>
                {c.authorName}
                {c.authorRole === 'ADMIN' ? ' · 사수' : ''}
              </p>
              <p style={{ margin: '2px 0 0', fontSize: 13 }}>{c.content}</p>
            </div>
          ))}
          {comments.length === 0 && (
            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>아직 댓글이 없어요.</p>
          )}
        </div>
      )}

      <div style={{ display: 'flex', gap: 6 }}>
        <input
          className="input"
          style={{ flex: 1 }}
          placeholder="댓글을 입력해주세요"
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSend()
          }}
        />
        <button
          className="btn-primary"
          style={{ width: 64 }}
          type="button"
          disabled={posting || !text.trim()}
          onClick={handleSend}
        >
          전송
        </button>
      </div>
    </div>
  )
}
