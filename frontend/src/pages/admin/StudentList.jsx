import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import StudentFilters from '../../components/students/StudentFilters'
import StudentTable from '../../components/students/StudentTable'
import studentService from '../../services/student.service'
import { matchesSearch } from '../../utils/search'

function StudentList() {
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [careerCode, setCareerCode] = useState('')

  useEffect(() => {
    studentService
      .getAll()
      .then((response) => setStudents(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const careerCodes = [...new Set(students.map((student) => student.careerCode))].sort()
  const isFiltered = Boolean(search || status || careerCode)
  const filteredStudents = students.filter(
    (student) =>
      matchesSearch(search, student.studentRun, student.studentFullname, student.studentEmail) &&
      (!status || student.studentStatus === status) &&
      (!careerCode || student.careerCode === careerCode),
  )

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
        title="Estudiantes"
        subtitle="Registro de estudiantes, su carrera y estado académico."
        action={<NewButton to="/admin/students/new">Nuevo estudiante</NewButton>}
      />
      <StudentFilters
        search={search}
        status={status}
        careerCode={careerCode}
        careerCodes={careerCodes}
        onSearchChange={setSearch}
        onStatusChange={setStatus}
        onCareerChange={setCareerCode}
      />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <StudentTable
        students={filteredStudents}
        loading={loading}
        isFiltered={isFiltered}
        onStatusChange={handleStatusChange}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar estudiante?"
        message={toDelete && `${toDelete.studentFullname} se eliminará de forma permanente, junto con su cuenta de acceso.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default StudentList
