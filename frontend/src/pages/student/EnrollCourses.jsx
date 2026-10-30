import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import PeriodFilter from '../../components/PeriodFilter'
import AvailableCourseTable from '../../components/enroll/AvailableCourseTable'
import EnrollmentSummaryBar from '../../components/enroll/EnrollmentSummaryBar'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'
import tuitionService from '../../services/tuition.service'

function EnrollCourses() {
  const navigate = useNavigate()
  const [periods, setPeriods] = useState([])
  const [tuitionPeriodIds, setTuitionPeriodIds] = useState([])
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [available, setAvailable] = useState([])
  const [selected, setSelected] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([academicPeriodService.getAll(), tuitionService.getMine()])
      .then(([periodsResponse, tuitionsResponse]) => {
        const openPeriods = periodsResponse.data.filter((period) => period.academicPeriodStatus === 'OPEN')
        setPeriods(openPeriods)
        setTuitionPeriodIds(tuitionsResponse.data.map((tuition) => tuition.academicPeriodId))
        if (openPeriods.length > 0) {
          setAcademicPeriodId(String(openPeriods[0].academicPeriodId))
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
    enrollmentService
      .getMyAvailable(academicPeriodId)
      .then((response) => setAvailable(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [academicPeriodId])

  const handlePeriodChange = (value) => {
    setLoading(true)
    setSelected([])
    setAcademicPeriodId(value)
  }

  const toggleCourse = (courseId) => {
    setSelected(selected.includes(courseId) ? selected.filter((item) => item !== courseId) : [...selected, courseId])
  }

  const handleSubmit = () => {
    setError('')
    setSaving(true)
    enrollmentService
      .create({ academicPeriodId: Number(academicPeriodId), courseIds: selected })
      .then(() => navigate('/student/enrollments'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  const hasOpenPeriods = periods.length > 0
  const hasTuition = tuitionPeriodIds.includes(Number(academicPeriodId))

  return (
    <>
      <PageHeader title="Inscribir ramos" subtitle="Elige un período abierto y los ramos que vas a tomar." />
      <ErrorAlert message={error} onClose={() => setError('')} />
      {!loading && !hasOpenPeriods ? (
        <Alert severity="info">No hay períodos académicos abiertos en este momento.</Alert>
      ) : (
        <>
          <Box sx={{ mb: 2 }}>
            <PeriodFilter
              periods={periods}
              value={academicPeriodId}
              onChange={handlePeriodChange}
              allLabel={null}
            />
          </Box>
          {academicPeriodId && !hasTuition && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              No tienes matrícula en este período, por lo que no puedes inscribir ramos. Contacta al administrador.
            </Alert>
          )}
          <AvailableCourseTable
            courses={available}
            selected={selected}
            loading={loading}
            onToggle={toggleCourse}
          />
          <EnrollmentSummaryBar
            count={selected.length}
            saving={saving}
            blocked={!hasTuition}
            onSubmit={handleSubmit}
          />
        </>
      )}
    </>
  )
}

export default EnrollCourses
