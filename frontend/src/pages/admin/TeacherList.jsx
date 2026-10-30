import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import TeacherTable from '../../components/teachers/TeacherTable'
import teacherService from '../../services/teacher.service'

function TeacherList() {
  const [teachers, setTeachers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    teacherService
      .getAll()
      .then((response) => setTeachers(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const handleToggleStatus = (teacher) => {
    const newStatus = teacher.teacherStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    setError('')
    teacherService
      .changeStatus(teacher.teacherId, newStatus)
      .then((response) =>
        setTeachers((current) =>
          current.map((item) => (item.teacherId === teacher.teacherId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = () => {
    const teacher = toDelete
    setToDelete(null)
    setError('')
    teacherService
      .remove(teacher.teacherId)
      .then(() => setTeachers((current) => current.filter((item) => item.teacherId !== teacher.teacherId)))
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader
        title="Teachers"
        subtitle="Faculty available to teach sections."
        action={<NewButton to="/admin/teachers/new">New teacher</NewButton>}
      />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <TeacherTable
        teachers={teachers}
        loading={loading}
        onToggleStatus={handleToggleStatus}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete teacher?"
        message={toDelete && `${toDelete.teacherFullname} will be permanently deleted, including their login account.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default TeacherList
