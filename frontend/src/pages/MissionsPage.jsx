import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useNotifications } from '../context/NotificationContext.jsx'
import MissionCommentThread from '../components/MissionCommentThread.jsx'

const STATUS_LABEL = {
  ASSIGNED: '진행 전',
  SUBMITTED: '검토 대기',
  APPROVED: '승인 완료',
  REJECTED: '반려됨',
}

function MissionCard({ mission, onSubmit, lastEvent }) {
  const [text, setText] = useState(mission.submissionText || '')
  const [submitting, setSubmitting] = useState(false)
  const editable = mission.status === 'ASSIGNED' || mission.status === 'REJECTED'

  const handleSubmit = async () => {
    if (!text.trim()) return
    setSubmitting(true)
    try {
      await onSubmit(mission.id, text)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="card" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
        <span className="badge-pill">{mission.weekNumber}주차</span>
        <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{STATUS_LABEL[mission.status]}</span>
      </div>
      <p style={{ fontSize: 15, fontWeight: 700, margin: '0 0 4px' }}>{mission.title}</p>
      <p style={{ fontSize: 13, color: 'var(--text-secondary)', margin: '0 0 12px' }}>{mission.description}</p>

      {editable ? (
        <>
          <textarea
            className="input"
            style={{ height: 80, padding: 10, resize: 'none', marginBottom: 8 }}
            placeholder="수행 내용을 입력해주세요"
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <button className="btn-primary" disabled={submitting || !text.trim()} onClick={handleSubmit} type="button">
            {submitting ? '제출 중...' : '제출하기'}
          </button>
        </>
      ) : (
        <div style={{ background: 'var(--surface-2)', borderRadius: 10, padding: 10, fontSize: 13 }}>
          <p style={{ margin: 0, color: 'var(--text-secondary)' }}>{mission.submissionText}</p>
          {mission.feedback && (
            <p style={{ margin: '8px 0 0', color: 'var(--brand-800)', fontWeight: 600 }}>
              인사팀: {mission.feedback}
            </p>
          )}
        </div>
      )}

      {mission.userMissionId && (
        <MissionCommentThread userMissionId={mission.userMissionId} lastEvent={lastEvent} />
      )}
    </div>
  )
}

export default function MissionsPage() {
  const navigate = useNavigate()
  const [missions, setMissions] = useState([])
  const [loading, setLoading] = useState(true)
  const { lastEvent } = useNotifications()

  const load = () => {
    apiClient.get('/api/missions').then((res) => setMissions(res.data)).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  // 사수가 승인/반려하면 화면을 새로고침하지 않아도 상태가 바로 반영되도록 한다.
  useEffect(() => {
    if (lastEvent && ['MISSION_APPROVED', 'MISSION_REJECTED'].includes(lastEvent.type)) {
      load()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastEvent])

  const handleSubmit = async (missionId, text) => {
    await apiClient.post(`/api/missions/${missionId}/submit`, { submissionText: text })
    load()
  }

  return (
    <div style={{ padding: '20px 16px 60px' }}>
      <button
        onClick={() => navigate('/')}
        style={{ background: 'none', border: 'none', marginBottom: 12, color: 'var(--text-secondary)', fontSize: 13 }}
        type="button"
      >
        <i className="ti ti-arrow-left" style={{ verticalAlign: -2, marginRight: 4 }} /> 홈으로
      </button>
      <p style={{ fontSize: 18, fontWeight: 700, margin: '0 0 16px' }}>주차별 미션</p>
      {loading ? (
        <p>불러오는 중...</p>
      ) : (
        missions.map((m) => (
          <MissionCard key={m.id} mission={m} onSubmit={handleSubmit} lastEvent={lastEvent} />
        ))
      )}
    </div>
  )
}
