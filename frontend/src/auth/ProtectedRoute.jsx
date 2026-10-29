import { Navigate, Outlet } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import useAuth from './useAuth'

function ProtectedRoute({ roles }) {
  const { me, hasRole } = useAuth()

  if (!roles.some(hasRole)) {
    return <Navigate to="/" replace />
  }

  const missingStudentRecord = roles.includes('STUDENT') && me.studentId == null
  const missingTeacherRecord = roles.includes('TEACHER') && me.teacherId == null

  if (missingStudentRecord || missingTeacherRecord) {
    return <Alert severity="warning">Your account has no linked record. Please contact the administrator.</Alert>
  }

  return <Outlet />
}

export default ProtectedRoute