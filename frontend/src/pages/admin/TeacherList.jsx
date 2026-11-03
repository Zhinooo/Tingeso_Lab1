import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import TeacherFilters from '../../components/teachers/TeacherFilters'
import TeacherTable from '../../components/teachers/TeacherTable'
import teacherService from '../../services/teacher.service'
import { matchesSearch } from '../../utils/search'

function TeacherList() {
  const [teachers, setTeachers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')

  useEffect(() => {
    teacherService
      .getAll()
      .then((response) => setTeachers(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const isFiltered = Boolean(search || status)
  const filteredTeachers = teachers.filter(
    (teacher) =>
      matchesSearch(search, teacher.teacherRun, teacher.teacherFullname, teacher.teacherEmail) &&
      (!status || teacher.teacherStatus === status),
  )

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
        title="Docentes"
        subtitle="Docentes disponibles para dictar secciones."
        action={<NewButton to="/admin/teachers/new">Nuevo docente</NewButton>}
      />
      <TeacherFilters search={search} status={status} onSearchChange={setSearch} onStatusChange={setStatus} />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <TeacherTable
        teachers={filteredTeachers}
        loading={loading}
        isFiltered={isFiltered}
        onToggleStatus={handleToggleStatus}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar docente?"
        message={toDelete && `${toDelete.teacherFullname} se eliminará de forma permanente, junto con su cuenta de acceso.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default TeacherList
