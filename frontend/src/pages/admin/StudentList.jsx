import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import StudentTable from '../../components/students/StudentTable'
import studentService from '../../services/student.service'

function StudentList() {
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    studentService
      .getAll()
      .then((response) => setStudents(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const handleStatusChange = (student, newStatus) => {
    setError('')
    studentService
      .changeStatus(student.studentId, newStatus)
      .then((response) =>
        setStudents((current) =>
          current.map((item) => (item.studentId === student.studentId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = () => {
    const student = toDelete
    setToDelete(null)
    setError('')
    studentService
      .remove(student.studentId)
      .then(() => setStudents((current) => current.filter((item) => item.studentId !== student.studentId)))
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader
        title="Students"
        subtitle="Student records, career and academic status."
        action={<NewButton to="/admin/students/new">New student</NewButton>}
      />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <StudentTable
        students={students}
        loading={loading}
        onStatusChange={handleStatusChange}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete student?"
        message={toDelete && `${toDelete.studentFullname} will be permanently deleted, including their login account.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default StudentList
