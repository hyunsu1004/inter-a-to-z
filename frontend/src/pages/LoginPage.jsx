import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function LoginPage() {
  const { login, signup } = useAuth()
  const navigate = useNavigate()
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [department, setDepartment] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (mode === 'login') {
        await login(email, password)
      } else {
        await signup(email, password, name, department)
      }
      navigate('/')
    } catch (err) {
      setError(err?.response?.data?.message || '로그인에 실패했어요. 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: '48px 24px', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <div style={{ textAlign: 'center', marginBottom: 40 }}>
        <div
          style={{
            width: 64,
            height: 64,
            borderRadius: 20,
            background: 'var(--brand-300)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 16px',
          }}
        >
          <i className="ti ti-rocket" style={{ fontSize: 30, color: '#FFF7EC' }} />
        </div>
        <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>INTER A to Z</h1>
        <p style={{ fontSize: 14, color: 'var(--text-secondary)', margin: '6px 0 0' }}>
          인터엑스 신규 입사자 온보딩 포털
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <button
          onClick={() => setMode('login')}
          className={mode === 'login' ? 'btn-primary' : 'btn-secondary'}
          style={{ flex: 1 }}
          type="button"
        >
          로그인
        </button>
        <button
          onClick={() => setMode('signup')}
          className={mode === 'signup' ? 'btn-primary' : 'btn-secondary'}
          style={{ flex: 1 }}
          type="button"
        >
          회원가입
        </button>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {mode === 'signup' && (
          <>
            <input
              className="input"
              placeholder="이름"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="부서/직무 (선택)"
              value={department}
              onChange={(e) => setDepartment(e.target.value)}
            />
          </>
        )}
        <input
          className="input"
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className="input"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {error && <p style={{ color: 'var(--danger)', fontSize: 13, margin: 0 }}>{error}</p>}
        <button className="btn-primary" type="submit" disabled={loading}>
          {loading ? '처리 중...' : mode === 'login' ? '로그인' : '가입하고 시작하기'}
        </button>
      </form>

      <div className="card" style={{ marginTop: 32, fontSize: 12, color: 'var(--text-secondary)' }}>
        <p style={{ margin: '0 0 6px', fontWeight: 600, color: 'var(--text-primary)' }}>심사용 테스트 계정</p>
        <p style={{ margin: '0 0 2px' }}>신입사원: newbie@interx.io / demo1234!</p>
        <p style={{ margin: 0 }}>인사팀 관리자: admin@interx.io / admin1234!</p>
      </div>
    </div>
  )
}
