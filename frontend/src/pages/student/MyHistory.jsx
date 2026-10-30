import { useEffect, useState } from 'react'
import ErrorAlert from '../../components/ErrorAlert'
import HistoryView from '../../components/HistoryView'
import PageHeader from '../../components/PageHeader'
import enrollmentService from '../../services/enrollment.service'
import studentService from '../../services/student.service'

function MyHistory() {
  const [student, setStudent] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.getMine(), enrollmentService.getMyHistory()])
      .then(([studentResponse, historyResponse]) => {
        setStudent(studentResponse.data)
        setHistory(historyResponse.data)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <PageHeader title="Historial académico" subtitle="Tus notas finales de los períodos cerrados." />
      <ErrorAlert message={error} />
      <HistoryView student={student} history={history} loading={loading} />
    </>
  )
}

export default MyHistory
