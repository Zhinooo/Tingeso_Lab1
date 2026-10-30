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

function EnrollCourses() {
  const navigate = useNavigate()
  const [periods, setPeriods] = useState([])
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [available, setAvailable] = useState([])
  const [selected, setSelected] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => {
        const openPeriods = response.data.filter((period) => period.academicPeriodStatus === 'OPEN')
        setPeriods(openPeriods)
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

  return (
    <>
      <PageHeader title="Enroll courses" subtitle="Choose an open period and pick the courses you will take." />
      <ErrorAlert message={error} onClose={() => setError('')} />
      {!loading && !hasOpenPeriods ? (
        <Alert severity="info">There are no open academic periods right now.</Alert>
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
          <AvailableCourseTable
            courses={available}
            selected={selected}
            loading={loading}
            onToggle={toggleCourse}
          />
          <EnrollmentSummaryBar count={selected.length} saving={saving} onSubmit={handleSubmit} />
        </>
      )}
    </>
  )
}

export default EnrollCourses
