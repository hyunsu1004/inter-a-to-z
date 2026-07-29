import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/AuthContext.jsx'

function statusStyle(status) {
  if (status === 'COMPLETED') {
    return { background: 'var(--brand-300)', color: '#FFF7EC' }
  }
  if (status === 'IN_PROGRESS') {
    return { background: 'var(--brand-50)', color: 'var(--brand-800)' }
  }
  return { background: 'var(--surface-2)', color: 'var(--text-secondary)' }
}

export default function DashboardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  const load = () => {
    apiClient.get('/api/dashboard').then((res) => setData(res.data)).finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  if (loading || !data) {
    return <div style={{ padding: 24 }}>불러오는 중...</div>
  }

  return (
    <div style={{ padding: '20px 16px 100px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 18 }}>
        <div>
          <p style={{ fontSize: 13, color: 'var(--text-secondary)', margin: 0 }}>안녕하세요</p>
          <p style={{ fontSize: 18, fontWeight: 700, margin: '2px 0 0' }}>{data.name}님</p>
        </div>
        <button
          onClick={() => {
            logout()
            navigate('/login')
          }}
          style={{
            background: 'transparent',
            border: '1px solid var(--border-strong)',
            borderRadius: 999,
            width: 38,
            height: 38,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          aria-label="로그아웃"
        >
          <i className="ti ti-logout" style={{ fontSize: 18, color: 'var(--text-secondary)' }} />
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginBottom: 16 }}>
        <div className="card" style={{ textAlign: 'center', padding: '14px 6px' }}>
          <i className="ti ti-flame" style={{ fontSize: 20, color: 'var(--brand-400)' }} />
          <p style={{ fontSize: 18, fontWeight: 700, margin: '6px 0 0' }}>{data.currentStreak}</p>
          <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: 0 }}>연속 접속</p>
        </div>
        <div className="card" style={{ textAlign: 'center', padding: '14px 6px' }}>
          <i className="ti ti-star" style={{ fontSize: 20, color: 'var(--brand-400)' }} />
          <p style={{ fontSize: 18, fontWeight: 700, margin: '6px 0 0' }}>{data.totalPoints}</p>
          <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: 0 }}>포인트</p>
        </div>
        <div className="card" style={{ textAlign: 'center', padding: '14px 6px' }}>
          <i className="ti ti-award" style={{ fontSize: 20, color: 'var(--brand-400)' }} />
          <p style={{ fontSize: 18, fontWeight: 700, margin: '6px 0 0' }}>{data.badgeCount}</p>
          <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: 0 }}>배지</p>
        </div>
      </div>

      {data.currentMission && (
        <div
          className="card"
          style={{ background: 'var(--brand-50)', border: 'none', marginBottom: 20, cursor: 'pointer' }}
          onClick={() => navigate('/missions')}
        >
          <p style={{ fontSize: 12, color: 'var(--brand-800)', margin: '0 0 4px', fontWeight: 600 }}>
            {data.currentMission.weekNumber}주차 미션
          </p>
          <p style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>{data.currentMission.title}</p>
        </div>
      )}

      <p style={{ fontSize: 14, fontWeight: 700, margin: '0 0 10px' }}>핵심가치 학습</p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
        {data.coreValues.map((cv) => (
          <button
            key={cv.id}
            onClick={() => navigate(`/values/${cv.id}`)}
            title={cv.name}
            style={{
              aspectRatio: '1',
              borderRadius: 14,
              border: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              ...statusStyle(cv.progressStatus),
            }}
          >
            <i className={`ti ti-${cv.icon}`} style={{ fontSize: 20 }} />
          </button>
        ))}
      </div>

      <button
        className="btn-secondary"
        style={{ marginTop: 20 }}
        onClick={() => navigate('/missions')}
        type="button"
      >
        미션 전체보기
      </button>
    </div>
  )
}
