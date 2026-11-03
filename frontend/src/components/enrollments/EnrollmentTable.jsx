import { NO_MATCH_HINT, NO_MATCH_MESSAGE } from '../../constants/search'
import DataTable from '../DataTable'
import EnrollmentRow from './EnrollmentRow'

const COLUMNS = [
  { label: 'RUN estudiante' },
  { label: 'Estudiante' },
  { label: 'Período' },
  { label: 'Código' },
  { label: 'Ramo' },
  { label: 'Fecha' },
  { label: 'Nota', align: 'center' },
  { label: 'Resultado' },
]

function EnrollmentTable({ enrollments, loading, isFiltered }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage={isFiltered ? NO_MATCH_MESSAGE : 'Aún no hay inscripciones'}
      emptyHint={isFiltered ? NO_MATCH_HINT : 'Los estudiantes inscriben sus ramos desde su propio portal.'}
    >
      {enrollments.map((enrollment) => (
        <EnrollmentRow key={enrollment.enrollmentId} enrollment={enrollment} />
      ))}
    </DataTable>
  )
}

export default EnrollmentTable
