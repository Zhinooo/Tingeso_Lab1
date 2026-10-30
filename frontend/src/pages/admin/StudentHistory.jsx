import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import HistoryView from '../../components/HistoryView'
import PageHeader from '../../components/PageHeader'
import enrollmentService from '../../services/enrollment.service'
import studentService from '../../services/student.service'

function StudentHistory() {
  const { id } = useParams()
  const [student, setStudent] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.get(id), enrollmentService.getStudentHistory(id)])
      .then(([studentResponse, historyResponse]) => {
        setStudent(studentResponse.data)
        setHistory(historyResponse.data)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  return (
    <>
      <PageHeader title="Academic history" backTo="/admin/students" />
      <ErrorAlert message={error} />
      <HistoryView student={student} history={history} loading={loading} />
    </>
  )
}

export default StudentHistory
