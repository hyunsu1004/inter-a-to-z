import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { AnimatePresence, motion } from 'framer-motion'
import apiClient from '../api/client'

export default function ValueLearningPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [coreValue, setCoreValue] = useState(null)
  const [cards, setCards] = useState([])
  const [step, setStep] = useState(0)
  const [selectedOption, setSelectedOption] = useState(null)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([
      apiClient.get('/api/core-values'),
      apiClient.get(`/api/core-values/${id}/cards`),
    ]).then(([listRes, cardsRes]) => {
      const cv = listRes.data.find((v) => String(v.id) === String(id))
      setCoreValue(cv)
      setCards(cardsRes.data)
    }).finally(() => setLoading(false))
  }, [id])

  if (loading) return <div style={{ padding: 24 }}>불러오는 중...</div>
  if (result) {
    return (
      <div style={{ padding: '20px 16px', minHeight: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <div
            style={{
              width: 72, height: 72, borderRadius: '50%',
              background: result.correct ? 'var(--brand-50)' : '#FBEAEA',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 18px',
            }}
          >
            <i
              className={`ti ${result.correct ? 'ti-award' : 'ti-mood-sad'}`}
              style={{ fontSize: 34, color: result.correct ? 'var(--brand-800)' : 'var(--danger)' }}
            />
          </div>
          <p style={{ fontSize: 18, fontWeight: 700, margin: '0 0 4px' }}>
            {result.correct ? '정답이에요!' : '다음엔 맞힐 수 있어요'}
          </p>
          {result.valueCompleted && (
            <p style={{ fontSize: 13, color: 'var(--text-secondary)', margin: '0 0 20px' }}>
              '{coreValue?.name}' 배지를 획득했어요
            </p>
          )}
          <div style={{ display: 'flex', justifyContent: 'center', gap: 20, margin: '20px 0' }}>
            <div>
              <p style={{ fontSize: 18, fontWeight: 700, margin: 0, color: 'var(--brand-800)' }}>
                +{result.pointsEarned}
              </p>
              <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: 0 }}>포인트</p>
            </div>
            <div>
              <p style={{ fontSize: 18, fontWeight: 700, margin: 0, color: 'var(--brand-800)' }}>
                {result.currentStreak}일째
              </p>
              <p style={{ fontSize: 11, color: 'var(--text-muted)', margin: 0 }}>연속 학습</p>
            </div>
          </div>
          {result.newBadges?.length > 0 && (
            <div style={{ marginBottom: 20 }}>
              {result.newBadges.map((b) => (
                <span key={b} className="badge-pill" style={{ marginRight: 6 }}>
                  <i className="ti ti-award" style={{ fontSize: 13 }} /> {b}
                </span>
              ))}
            </div>
          )}
          <button className="btn-primary" onClick={() => navigate('/')} type="button">
            홈으로
          </button>
        </div>
      </div>
    )
  }

  const card = cards[step]
  const isQuiz = card?.cardType === 'QUIZ'

  const handleNext = () => {
    if (step < cards.length - 1) {
      setStep(step + 1)
      setSelectedOption(null)
    }
  }

  const handleSubmitQuiz = async () => {
    if (!selectedOption) return
    setSubmitting(true)
    try {
      const { data } = await apiClient.post('/api/core-values/quiz/submit', {
        cardId: card.id,
        selectedOptionId: selectedOption,
      })
      setResult(data)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      <button
        onClick={() => navigate('/')}
        style={{ background: 'none', border: 'none', marginBottom: 12, color: 'var(--text-secondary)', fontSize: 13 }}
        type="button"
      >
        <i className="ti ti-arrow-left" style={{ verticalAlign: -2, marginRight: 4 }} /> 홈으로
      </button>

      <div style={{ display: 'flex', gap: 4, marginBottom: 16 }}>
        {cards.map((c, i) => (
          <div
            key={c.id}
            style={{
              height: 4, flex: 1, borderRadius: 2,
              background: i <= step ? 'var(--brand-300)' : 'var(--border)',
            }}
          />
        ))}
      </div>

      <AnimatePresence mode="wait">
        <motion.div
          key={step}
          initial={{ opacity: 0, x: 24 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -24 }}
          transition={{ duration: 0.25 }}
          className="card"
          style={{ minHeight: 320, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}
        >
          <div>
            <div
              style={{
                width: 44, height: 44, borderRadius: 14, background: 'var(--brand-50)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 16,
              }}
            >
              <i className={`ti ti-${coreValue?.icon}`} style={{ fontSize: 22, color: 'var(--brand-800)' }} />
            </div>
            <p style={{ fontSize: 12, color: 'var(--text-secondary)', margin: '0 0 4px' }}>
              핵심가치 {String(coreValue?.sortOrder).padStart(2, '0')} · {coreValue?.name}
            </p>
            <p style={{ fontSize: 16, lineHeight: 1.6, fontWeight: card.cardType === 'INTRO' ? 700 : 400, margin: 0 }}>
              {card.content}
            </p>

            {isQuiz && (
              <div style={{ marginTop: 18, display: 'flex', flexDirection: 'column', gap: 8 }}>
                {card.options.map((opt) => (
                  <button
                    key={opt.id}
                    onClick={() => setSelectedOption(opt.id)}
                    type="button"
                    style={{
                      textAlign: 'left',
                      padding: '12px 14px',
                      borderRadius: 12,
                      border: selectedOption === opt.id ? '2px solid var(--brand-300)' : '1px solid var(--border-strong)',
                      background: selectedOption === opt.id ? 'var(--brand-50)' : 'var(--surface-1)',
                      fontSize: 14,
                    }}
                  >
                    {opt.text}
                  </button>
                ))}
              </div>
            )}
          </div>

          {isQuiz ? (
            <button
              className="btn-primary"
              style={{ marginTop: 20 }}
              disabled={!selectedOption || submitting}
              onClick={handleSubmitQuiz}
              type="button"
            >
              {submitting ? '채점 중...' : '제출'}
            </button>
          ) : (
            <button className="btn-primary" style={{ marginTop: 20 }} onClick={handleNext} type="button">
              다음
            </button>
          )}
        </motion.div>
      </AnimatePresence>
    </div>
  )
}
