import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import RadarChart from '../components/RadarChart.jsx'

const TYPE_ICONS = {
  QUIZ_CORRECT: 'help-circle',
  VALUE_COMPLETE: 'flag',
  MISSION_SUBMIT: 'send',
  MISSION_APPROVED: 'circle-check',
}

function formatDate(iso) {
  const d = new Date(iso)
  return `${d.getMonth() + 1}월 ${d.getDate()}일`
}

export default function GrowthPage() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    apiClient
      .get('/api/growth')
      .then((res) => setData(res.data))
      .finally(() => setLoading(false))
  }, [])

  if (loading || !data) {
    return <div style={{ padding: 24 }}>불러오는 중...</div>
  }

  const radarData = data.radar.map((r) => ({ name: r.name, score: r.score }))

  return (
    <div style={{ padding: '20px 16px 100px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 18 }}>
        <button
          onClick={() => navigate(-1)}
          style={{ background: 'transparent', border: 'none', padding: 0, display: 'flex' }}
          aria-label="뒤로가기"
        >
          <i className="ti ti-chevron-left" style={{ fontSize: 22, color: 'var(--text-primary)' }} />
        </button>
        <p style={{ fontSize: 18, fontWeight: 700, margin: 0 }}>나의 성장</p>
      </div>

      <div
        className="card"
        style={{ display: 'flex', justifyContent: 'center', padding: '20px 8px', marginBottom: 20 }}
      >
        <RadarChart data={radarData} size={280} />
      </div>

      <p style={{ fontSize: 14, fontWeight: 700, margin: '0 0 10px' }}>활동 타임라인</p>
      {data.timeline.length === 0 ? (
        <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>
          아직 활동 기록이 없어요. 첫 핵심가치를 학습해보세요!
        </p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {data.timeline.map((entry, idx) => (
            <div
              key={idx}
              className="card"
              style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px' }}
            >
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: 999,
                  background: 'var(--brand-50)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <i
                  className={`ti ti-${TYPE_ICONS[entry.type] || 'sparkles'}`}
                  style={{ fontSize: 16, color: 'var(--brand-400)' }}
                />
              </div>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: 13, fontWeight: 600, margin: 0 }}>{entry.label}</p>
                <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: '2px 0 0' }}>
                  {formatDate(entry.occurredAt)}
                </p>
              </div>
              <span className="badge-pill">+{entry.points}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
