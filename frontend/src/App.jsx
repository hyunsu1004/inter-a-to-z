import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import LoginPage from './pages/LoginPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import ValueLearningPage from './pages/ValueLearningPage.jsx'
import MissionsPage from './pages/MissionsPage.jsx'
import GrowthPage from './pages/GrowthPage.jsx'
import AdminPage from './pages/AdminPage.jsx'
import Chatbot from './components/Chatbot.jsx'

function ProtectedRoute({ children, adminOnly = false }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { user } = useAuth()

  return (
    <div className="app-shell">
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/values/:id"
          element={
            <ProtectedRoute>
              <ValueLearningPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/missions"
          element={
            <ProtectedRoute>
              <MissionsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/growth"
          element={
            <ProtectedRoute>
              <GrowthPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute adminOnly>
              <AdminPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      {user && user.role !== 'ADMIN' && <Chatbot />}
    </div>
  )
}
