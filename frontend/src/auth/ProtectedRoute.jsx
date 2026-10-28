import { Navigate, Outlet } from 'react-router-dom'
import useAuth from './useAuth'

function ProtectedRoute({ roles }) {
  const { hasRole } = useAuth()

  if (!roles.some(hasRole)) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

export default ProtectedRoute