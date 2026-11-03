import { NO_MATCH_HINT, NO_MATCH_MESSAGE } from '../../constants/search'
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

function TeacherTable({ teachers, loading, isFiltered, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={teachers.length}
      loading={loading}
      emptyMessage={isFiltered ? NO_MATCH_MESSAGE : 'Aún no hay docentes'}
      emptyHint={isFiltered ? NO_MATCH_HINT : 'Agrega un docente para poder asignarle secciones.'}
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
