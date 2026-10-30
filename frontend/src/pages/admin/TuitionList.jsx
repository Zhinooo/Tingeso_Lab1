import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import PeriodFilter from '../../components/PeriodFilter'
import TuitionTable from '../../components/tuitions/TuitionTable'
import academicPeriodService from '../../services/academicPeriod.service'
import studentService from '../../services/student.service'
import tuitionService from '../../services/tuition.service'
import { formatPeriod } from '../../utils/periods'

function TuitionList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const academicPeriodId = searchParams.get('academicPeriodId') ?? ''
  const [periods, setPeriods] = useState([])
  const [students, setStudents] = useState([])
  const [tuitions, setTuitions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    Promise.all([academicPeriodService.getAll(), studentService.getAll()])
      .then(([periodsResponse, studentsResponse]) => {
        setPeriods(periodsResponse.data)
        setStudents(studentsResponse.data)
      })
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? tuitionService.getByAcademicPeriod(academicPeriodId)
      : tuitionService.getAll()
    request
      .then((response) => setTuitions(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [academicPeriodId])

  const studentNames = Object.fromEntries(students.map((student) => [student.studentId, student.studentFullname]))

  const handlePeriodChange = (value) => {
    setLoading(true)
    setSearchParams(value ? { academicPeriodId: value } : {})
  }

  const handleDelete = () => {
    const tuition = toDelete
    setToDelete(null)
    setError('')
    tuitionService
      .remove(tuition.tuitionId)
      .then(() => setTuitions((current) => current.filter((item) => item.tuitionId !== tuition.tuitionId)))
      .catch((err) => setError(err.message))
  }

  const newTuitionUrl = academicPeriodId
    ? `/admin/tuitions/new?academicPeriodId=${academicPeriodId}`
    : '/admin/tuitions/new'

  return (
    <>
      <PageHeader
        title="Matrículas"
        subtitle="Estudiantes matriculados en cada período académico."
        action={<NewButton to={newTuitionUrl}>Nueva matrícula</NewButton>}
      />
      <Box sx={{ mb: 2 }}>
        <PeriodFilter periods={periods} value={academicPeriodId} onChange={handlePeriodChange} />
      </Box>
      <ErrorAlert message={error} onClose={() => setError('')} />
      <TuitionTable
        tuitions={tuitions}
        studentNames={studentNames}
        loading={loading}
        emptyHint={academicPeriodId ? 'Aún no hay estudiantes matriculados en este período.' : 'Matricula a un estudiante en un período abierto.'}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar matrícula?"
        message={toDelete && `Se eliminará la matrícula de ${toDelete.studentRun} en ${formatPeriod(toDelete)}.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default TuitionList
