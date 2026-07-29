import { useNotifications } from '../context/NotificationContext.jsx'

const TYPE_ICON = {
  MISSION_SUBMITTED: 'send',
  MISSION_APPROVED: 'circle-check',
  MISSION_REJECTED: 'circle-x',
  NEW_COMMENT: 'message-circle',
}

export default function ToastHost() {
  const { toasts, dismissToast } = useNotifications()

  if (toasts.length === 0) return null

  return (
    <div
      style={{
        position: 'fixed',
        top: 16,
        right: 16,
        left: 16,
        zIndex: 999,
        display: 'flex',
        flexDirection: 'column',
        gap: 8,
        maxWidth: 360,
        marginLeft: 'auto',
      }}
    >
      {toasts.map((t) => (
        <div
          key={t.id}
          className="card"
          style={{
            display: 'flex',
            gap: 10,
            alignItems: 'flex-start',
            padding: '12px 14px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.16)',
            cursor: 'pointer',
          }}
          onClick={() => dismissToast(t.id)}
          role="alert"
        >
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: 999,
              background: 'var(--brand-50)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <i className={`ti ti-${TYPE_ICON[t.type] || 'bell'}`} style={{ fontSize: 14, color: 'var(--brand-400)' }} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: 13, fontWeight: 700 }}>{t.title}</p>
            <p style={{ margin: '2px 0 0', fontSize: 12, color: 'var(--text-secondary)' }}>{t.message}</p>
          </div>
        </div>
      ))}
    </div>
  )
}
