import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Stack from '@mui/material/Stack'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import StudyplanFilter from '../../components/StudyplanFilter'
import CourseTable from '../../components/courses/CourseTable'
import SemesterLegend from '../../components/courses/SemesterLegend'
import courseService from '../../services/course.service'
import studyplanService from '../../services/studyplan.service'

function CourseList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const studyplanId = searchParams.get('studyplanId') ?? ''
  const [studyplans, setStudyplans] = useState([])
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    studyplanService
      .getAll()
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = studyplanId ? courseService.getByStudyplan(studyplanId) : courseService.getAll()
    request
      .then((response) => setCourses(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [studyplanId])

  const handleStudyplanChange = (value) => {
    setLoading(true)
    setSearchParams(value ? { studyplanId: value } : {})
  }

  const handleDelete = () => {
    const course = toDelete
    setToDelete(null)
    setError('')
    courseService
      .remove(course.courseId)
      .then(() => setCourses((current) => current.filter((item) => item.courseId !== course.courseId)))
      .catch((err) => setError(err.message))
  }

  const newCourseUrl = studyplanId ? `/admin/courses/new?studyplanId=${studyplanId}` : '/admin/courses/new'

  return (
    <>
      <PageHeader
        title="Ramos"
        subtitle="Ramos por semestre, con sus horas semanales y prerrequisitos."
        action={<NewButton to={newCourseUrl}>Nuevo ramo</NewButton>}
      />
      <Stack direction="row" spacing={3} sx={{ mb: 2, alignItems: 'center' }}>
        <StudyplanFilter studyplans={studyplans} value={studyplanId} onChange={handleStudyplanChange} />
        <SemesterLegend />
      </Stack>
      <ErrorAlert message={error} onClose={() => setError('')} />
      <CourseTable
        courses={courses}
        loading={loading}
        emptyHint={studyplanId ? 'Este plan de estudio aún no tiene ramos.' : 'Primero crea un plan de estudio y luego agrega sus ramos.'}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar ramo?"
        message={toDelete && `${toDelete.courseCode} · ${toDelete.courseName} se eliminará de forma permanente.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default CourseList
