import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/AuthContext.jsx'

const TABS = [
  { key: 'users', label: '진행현황' },
  { key: 'submissions', label: '미션 검토' },
  { key: 'feedbacks', label: '피드백함' },
]

function UsersTab({ users }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {users.map((u) => (
        <div key={u.userId} className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <p style={{ margin: 0, fontWeight: 700, fontSize: 14 }}>{u.name}</p>
            <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{u.department}</span>
          </div>
          <p style={{ margin: '2px 0 10px', fontSize: 12, color: 'var(--text-muted)' }}>{u.email}</p>
          <div style={{ display: 'flex', gap: 14, fontSize: 12, color: 'var(--text-secondary)' }}>
            <span>가치 완료 {u.completedValues}/12</span>
            <span>포인트 {u.totalPoints}</span>
            <span>스트릭 {u.currentStreak}일</span>
            <span>검토대기 {u.pendingMissions}</span>
          </div>
        </div>
      ))}
      {users.length === 0 && <p style={{ color: 'var(--text-muted)' }}>아직 신입사원이 없어요.</p>}
    </div>
  )
}

function SubmissionsTab({ submissions, userMap, missionMap, onReview }) {
  const pending = submissions.filter((s) => s.status === 'SUBMITTED')
  const [feedbackDraft, setFeedbackDraft] = useState({})

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {pending.map((s) => (
        <div key={s.id} className="card">
          <p style={{ margin: 0, fontSize: 12, color: 'var(--text-secondary)' }}>
            {userMap[s.userId] || `사용자 #${s.userId}`} · {missionMap[s.missionId] || `미션 #${s.missionId}`}
          </p>
          <p style={{ margin: '8px 0', fontSize: 14, background: 'var(--surface-2)', borderRadius: 10, padding: 10 }}>
            {s.submissionText}
          </p>
          <textarea
            className="input"
            style={{ height: 60, padding: 8, resize: 'none', marginBottom: 8 }}
            placeholder="피드백을 남겨주세요 (선택)"
            value={feedbackDraft[s.id] || ''}
            onChange={(e) => setFeedbackDraft((prev) => ({ ...prev, [s.id]: e.target.value }))}
          />
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-primary" type="button" onClick={() => onReview(s.id, true, feedbackDraft[s.id] || '')}>
              승인
            </button>
            <button className="btn-secondary" type="button" onClick={() => onReview(s.id, false, feedbackDraft[s.id] || '')}>
              반려
            </button>
          </div>
        </div>
      ))}
      {pending.length === 0 && <p style={{ color: 'var(--text-muted)' }}>검토 대기 중인 제출이 없어요.</p>}
    </div>
  )
}

function FeedbacksTab({ feedbacks, userMap, onReply }) {
  const [replyDraft, setReplyDraft] = useState({})
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {feedbacks.map((f) => (
        <div key={f.id} className="card">
          <p style={{ margin: 0, fontSize: 12, color: 'var(--text-secondary)' }}>
            {userMap[f.userId] || `사용자 #${f.userId}`}
          </p>
          <p style={{ margin: '8px 0', fontSize: 14 }}>{f.content}</p>
          {f.reply ? (
            <p style={{ margin: 0, fontSize: 13, color: 'var(--brand-800)', fontWeight: 600 }}>
              답변: {f.reply}
            </p>
          ) : (
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                className="input"
                style={{ flex: 1 }}
                placeholder="답변 입력"
                value={replyDraft[f.id] || ''}
                onChange={(e) => setReplyDraft((prev) => ({ ...prev, [f.id]: e.target.value }))}
              />
              <button
                className="btn-primary"
                style={{ width: 80 }}
                type="button"
                onClick={() => onReply(f.id, replyDraft[f.id] || '')}
              >
                답변
              </button>
            </div>
          )}
        </div>
      ))}
      {feedbacks.length === 0 && <p style={{ color: 'var(--text-muted)' }}>등록된 피드백이 없어요.</p>}
    </div>
  )
}

export default function AdminPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [tab, setTab] = useState('users')
  const [users, setUsers] = useState([])
  const [submissions, setSubmissions] = useState([])
  const [missions, setMissions] = useState([])
  const [feedbacks, setFeedbacks] = useState([])

  const loadAll = () => {
    apiClient.get('/api/admin/users').then((r) => setUsers(r.data))
    apiClient.get('/api/admin/missions/submissions').then((r) => setSubmissions(r.data))
    apiClient.get('/api/admin/missions').then((r) => setMissions(r.data))
    apiClient.get('/api/admin/feedbacks').then((r) => setFeedbacks(r.data))
  }

  useEffect(() => { loadAll() }, [])

  const userMap = useMemo(() => Object.fromEntries(users.map((u) => [u.userId, u.name])), [users])
  const missionMap = useMemo(() => Object.fromEntries(missions.map((m) => [m.id, m.title])), [missions])

  const handleReview = async (id, approved, feedback) => {
    await apiClient.post(`/api/admin/missions/submissions/${id}/review`, { approved, feedback })
    loadAll()
  }

  const handleReply = async (id, reply) => {
    await apiClient.post(`/api/admin/feedbacks/${id}/reply`, { reply })
    loadAll()
  }

  return (
    <div style={{ padding: '20px 16px 60px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <p style={{ fontSize: 18, fontWeight: 700, margin: 0 }}>인사팀 대시보드</p>
        <button
          onClick={() => { logout(); navigate('/login') }}
          style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', fontSize: 13 }}
          type="button"
        >
          로그아웃
        </button>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={tab === t.key ? 'btn-primary' : 'btn-secondary'}
            style={{ flex: 1 }}
            type="button"
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'users' && <UsersTab users={users} />}
      {tab === 'submissions' && (
        <SubmissionsTab submissions={submissions} userMap={userMap} missionMap={missionMap} onReview={handleReview} />
      )}
      {tab === 'feedbacks' && <FeedbacksTab feedbacks={feedbacks} userMap={userMap} onReply={handleReply} />}
    </div>
  )
}
