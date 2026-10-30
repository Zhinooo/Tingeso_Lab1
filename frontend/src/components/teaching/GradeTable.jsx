import DataTable from '../DataTable'
import GradeRow from './GradeRow'

const COLUMNS = [
  { label: 'RUN estudiante' },
  { label: 'Nota final' },
  { label: 'Resultado' },
  { label: '', align: 'right' },
]

function GradeTable({ enrollments, grades, loading, readOnly, savingId, onChange, onSave }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage="Aún no hay estudiantes inscritos"
    >
      {enrollments.map((enrollment) => (
        <GradeRow
          key={enrollment.enrollmentId}
          enrollment={enrollment}
          value={grades[enrollment.enrollmentId] ?? ''}
          readOnly={readOnly}
          saving={savingId === enrollment.enrollmentId}
          onChange={onChange}
          onSave={onSave}
        />
      ))}
    </DataTable>
  )
}

export default GradeTable
