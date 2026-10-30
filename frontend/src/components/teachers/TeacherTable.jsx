import DataTable from '../DataTable'
import TeacherRow from './TeacherRow'

const COLUMNS = [
  { label: 'RUN' },
  { label: 'Nombre completo' },
  { label: 'Correo' },
  { label: 'Título profesional' },
  { label: 'Grado' },
  { label: 'Estado' },
  { label: 'Acciones', align: 'right' },
]

function TeacherTable({ teachers, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={teachers.length}
      loading={loading}
      emptyMessage="Aún no hay docentes"
      emptyHint="Agrega un docente para poder asignarle secciones."
    >
      {teachers.map((teacher) => (
        <TeacherRow
          key={teacher.teacherId}
          teacher={teacher}
          onToggleStatus={onToggleStatus}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default TeacherTable
