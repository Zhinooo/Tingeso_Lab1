import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import MyEnrollmentTable from '../../components/enrollments/MyEnrollmentTable'
import enrollmentService from '../../services/enrollment.service'

function MyEnrollments() {
  const [enrollments, setEnrollments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toCancel, setToCancel] = useState(null)

  useEffect(() => {
    enrollmentService
      .getMine()
      .then((response) => setEnrollments(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const handleCancel = () => {
    const enrollment = toCancel
    setToCancel(null)
    setError('')
    enrollmentService
      .cancel(enrollment.enrollmentId)
      .then(() =>
        setEnrollments((current) => current.filter((item) => item.enrollmentId !== enrollment.enrollmentId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader title="My enrollments" action={<NewButton to="/student/enroll">Enroll courses</NewButton>} />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <MyEnrollmentTable enrollments={enrollments} loading={loading} onCancel={setToCancel} />
      <ConfirmDialog
        open={Boolean(toCancel)}
        title="Cancel enrollment?"
        message={toCancel && `You will be removed from ${toCancel.courseCode} · ${toCancel.courseName}.`}
        confirmLabel="Cancel enrollment"
        onConfirm={handleCancel}
        onClose={() => setToCancel(null)}
      />
    </>
  )
}

export default MyEnrollments
