import DataTable from '../DataTable'
import StudentRow from './StudentRow'

const COLUMNS = [
  { label: 'RUN' },
  { label: 'Nombre completo' },
  { label: 'Correo' },
  { label: 'Carrera' },
  { label: 'Plan de estudio' },
  { label: 'Estado' },
  { label: 'Acciones', align: 'right' },
]

function StudentTable({ students, loading, onStatusChange, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={students.length}
      loading={loading}
      emptyMessage="Aún no hay estudiantes"
      emptyHint="Los estudiantes quedan en el plan vigente de su carrera."
    >
      {students.map((student) => (
        <StudentRow
          key={student.studentId}
          student={student}
          onStatusChange={onStatusChange}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default StudentTable
