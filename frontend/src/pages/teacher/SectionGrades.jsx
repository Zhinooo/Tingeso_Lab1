import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Snackbar from '@mui/material/Snackbar'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import GradeTable from '../../components/teaching/GradeTable'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'
import sectionService from '../../services/section.service'
import { formatPeriod } from '../../utils/periods'

function SectionGrades() {
  const { id } = useParams()
  const [section, setSection] = useState(null)
  const [isClosed, setIsClosed] = useState(false)
  const [enrollments, setEnrollments] = useState([])
  const [grades, setGrades] = useState({})
  const [loading, setLoading] = useState(true)
  const [savingId, setSavingId] = useState(null)
  const [error, setError] = useState('')
  const [savedMessage, setSavedMessage] = useState('')

  useEffect(() => {
    Promise.all([
      sectionService.get(id),
      enrollmentService.getMySectionEnrollments(id),
      academicPeriodService.getAll(),
    ])
      .then(([sectionResponse, enrollmentsResponse, periodsResponse]) => {
        const loadedSection = sectionResponse.data
        const period = periodsResponse.data.find(
          (item) => item.academicPeriodId === loadedSection.academicPeriodId,
        )
        setSection(loadedSection)
        setIsClosed(period?.academicPeriodStatus === 'CLOSED')
        setEnrollments(enrollmentsResponse.data)
        setGrades(
          Object.fromEntries(
            enrollmentsResponse.data.map((enrollment) => [enrollment.enrollmentId, enrollment.finalGrade ?? '']),
          ),
        )
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  const handleGradeChange = (enrollmentId, value) => {
    setGrades({ ...grades, [enrollmentId]: value })
  }

  const handleSave = (enrollment) => {
    setError('')
    setSavingId(enrollment.enrollmentId)
    enrollmentService
      .registerGrade(enrollment.enrollmentId, { finalGrade: Number(grades[enrollment.enrollmentId]) })
      .then((response) => {
        setEnrollments((current) =>
          current.map((item) => (item.enrollmentId === enrollment.enrollmentId ? response.data : item)),
        )
        setSavedMessage(`Nota guardada para ${enrollment.studentRun}`)
      })
      .catch((err) => setError(err.message))
      .finally(() => setSavingId(null))
  }

  const title = section ? `${section.courseCode} · ${section.courseName}` : 'Sección'
  const subtitle = section
    ? `Período ${formatPeriod(section)} · ${enrollments.length} estudiantes${isClosed ? ' · Período cerrado (solo lectura)' : ''}`
    : undefined

  return (
    <>
      <PageHeader title={title} subtitle={subtitle} backTo="/teacher/sections" />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <GradeTable
        enrollments={enrollments}
        grades={grades}
        loading={loading}
        readOnly={isClosed}
        savingId={savingId}
        onChange={handleGradeChange}
        onSave={handleSave}
      />
      <Snackbar
        open={Boolean(savedMessage)}
        autoHideDuration={2500}
        onClose={() => setSavedMessage('')}
        message={savedMessage}
      />
    </>
  )
}

export default SectionGrades
