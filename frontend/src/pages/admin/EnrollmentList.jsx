import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Stack from '@mui/material/Stack'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import PeriodFilter from '../../components/PeriodFilter'
import SearchField from '../../components/SearchField'
import EnrollmentTable from '../../components/enrollments/EnrollmentTable'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'
import { matchesSearch } from '../../utils/search'

function EnrollmentList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const academicPeriodId = searchParams.get('academicPeriodId') ?? ''
  const [periods, setPeriods] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? enrollmentService.getByAcademicPeriod(academicPeriodId)
      : enrollmentService.getAll()
    request
      .then((response) => setEnrollments(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [academicPeriodId])

  const filteredEnrollments = enrollments.filter((enrollment) =>
    matchesSearch(search, enrollment.studentRun, enrollment.studentFullname, enrollment.courseCode, enrollment.courseName),
  )

  const handlePeriodChange = (value) => {
    setLoading(true)
    setSearchParams(value ? { academicPeriodId: value } : {})
  }

  return (
    <>
      <PageHeader title="Inscripciones" subtitle="Vista de solo lectura de las inscripciones y notas finales." />
      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <PeriodFilter periods={periods} value={academicPeriodId} onChange={handlePeriodChange} />
        <SearchField value={search} placeholder="Buscar por RUN, estudiante o ramo" onChange={setSearch} />
      </Stack>
      <ErrorAlert message={error} onClose={() => setError('')} />
      <EnrollmentTable enrollments={filteredEnrollments} loading={loading} isFiltered={Boolean(search)} />
    </>
  )
}

export default EnrollmentList
