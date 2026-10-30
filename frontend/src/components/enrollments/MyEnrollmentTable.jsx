import { comparePeriodsDesc } from '../../utils/periods'
import DataTable from '../DataTable'
import MyEnrollmentRow from './MyEnrollmentRow'

const COLUMNS = [
  { label: 'Período' },
  { label: 'Código' },
  { label: 'Ramo' },
  { label: 'Fecha' },
  { label: 'Nota', align: 'center' },
  { label: 'Resultado' },
  { label: 'Acciones', align: 'right' },
]

function MyEnrollmentTable({ enrollments, loading, onCancel }) {
  const sortedEnrollments = [...enrollments].sort(comparePeriodsDesc)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage="Aún no tienes inscripciones"
      emptyHint="Inscribe ramos en un período abierto en el que tengas matrícula."
    >
      {sortedEnrollments.map((enrollment) => (
        <MyEnrollmentRow key={enrollment.enrollmentId} enrollment={enrollment} onCancel={onCancel} />
      ))}
    </DataTable>
  )
}

export default MyEnrollmentTable
