import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import EmptyState from '../../components/EmptyState'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import PeriodFilter from '../../components/PeriodFilter'
import WeeklySchedule from '../../components/schedule/WeeklySchedule'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'
import sectionService from '../../services/section.service'
import { comparePeriodsDesc } from '../../utils/periods'

function MySchedule() {
  const [periods, setPeriods] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([academicPeriodService.getAll(), enrollmentService.getMine()])
      .then(([periodsResponse, enrollmentsResponse]) => {
        const enrolledPeriodIds = new Set(enrollmentsResponse.data.map((enrollment) => enrollment.academicPeriodId))
        const myPeriods = periodsResponse.data
          .filter((period) => enrolledPeriodIds.has(period.academicPeriodId))
          .sort(comparePeriodsDesc)
        const defaultPeriod = myPeriods.find((period) => period.academicPeriodStatus === 'OPEN') ?? myPeriods[0]
        setPeriods(myPeriods)
        setEnrollments(enrollmentsResponse.data)
        if (defaultPeriod) {
          setAcademicPeriodId(String(defaultPeriod.academicPeriodId))
        } else {
          setLoading(false)
        }
      })
      .catch((err) => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  useEffect(() => {
    if (!academicPeriodId) return
    sectionService
      .getByAcademicPeriod(academicPeriodId)
      .then((response) => {
        const blocksBySection = Object.fromEntries(
          response.data.map((section) => [section.sectionId, section.scheduleBlocks]),
        )
        setEntries(
          enrollments
            .filter((enrollment) => enrollment.academicPeriodId === Number(academicPeriodId))
            .map((enrollment) => ({
              courseCode: enrollment.courseCode,
              courseName: enrollment.courseName,
              blocks: blocksBySection[enrollment.sectionId] ?? [],
            })),
        )
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [academicPeriodId, enrollments])

  const handlePeriodChange = (value) => {
    setLoading(true)
    setAcademicPeriodId(value)
  }

  return (
    <>
      <PageHeader title="Mi horario" subtitle="Tus ramos inscritos, distribuidos en la semana." />
      <ErrorAlert message={error} />
      {loading && <LinearProgress sx={{ mb: 2 }} />}
      {!loading && periods.length === 0 && (
        <Paper>
          <EmptyState
            message="Aún no tienes ramos inscritos"
            hint="Cuando inscribas ramos, tu horario aparecerá aquí."
            action={<NewButton to="/student/enroll">Inscribir ramos</NewButton>}
          />
        </Paper>
      )}
      {periods.length > 0 && (
        <>
          <Box sx={{ mb: 2 }}>
            <PeriodFilter periods={periods} value={academicPeriodId} onChange={handlePeriodChange} allLabel={null} />
          </Box>
          <WeeklySchedule entries={entries} />
        </>
      )}
    </>
  )
}

export default MySchedule
