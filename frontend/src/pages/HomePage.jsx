import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import useAuth from '../auth/useAuth'
import ErrorAlert from '../components/ErrorAlert'
import AdminSummary from '../components/home/AdminSummary'
import { formatRoles } from '../constants/roles'
import academicPeriodService from '../services/academicPeriod.service'
import enrollmentService from '../services/enrollment.service'
import sectionService from '../services/section.service'
import studentService from '../services/student.service'
import { buildAdminSummary, findCurrentPeriod } from '../utils/adminSummary'

function HomePage() {
  const { me, hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isAdmin) return
    Promise.all([academicPeriodService.getAll(), studentService.getAll()])
      .then(([periodsResponse, studentsResponse]) => {
        const currentPeriod = findCurrentPeriod(periodsResponse.data)
        const periodData = currentPeriod
          ? Promise.all([
              sectionService.getByAcademicPeriod(currentPeriod.academicPeriodId),
              enrollmentService.getByAcademicPeriod(currentPeriod.academicPeriodId),
            ])
          : Promise.resolve([{ data: [] }, { data: [] }])
        return periodData.then(([sectionsResponse, enrollmentsResponse]) =>
          setSummary(
            buildAdminSummary({
              periods: periodsResponse.data,
              students: studentsResponse.data,
              sections: sectionsResponse.data,
              enrollments: enrollmentsResponse.data,
            }),
          ),
        )
      })
      .catch((err) => setError(err.message))
  }, [isAdmin])

  return (
    <>
      <Paper sx={{ p: 4, mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Bienvenido/a, {me.fullName}
        </Typography>
        <Typography color="text.secondary">
          Ingresaste como {formatRoles(me.roles)}. Usa el menú de la izquierda para comenzar.
        </Typography>
      </Paper>
      {isAdmin && (
        <Box>
          <ErrorAlert message={error} />
          {!summary && !error && <LinearProgress />}
          {summary && <AdminSummary summary={summary} />}
        </Box>
      )}
    </>
  )
}

export default HomePage
