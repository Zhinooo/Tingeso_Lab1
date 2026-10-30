import DataTable from '../DataTable'
import GradeRow from './GradeRow'

const COLUMNS = [
  { label: 'Student RUN' },
  { label: 'Final grade' },
  { label: 'Result' },
  { label: '', align: 'right' },
]

function GradeTable({ enrollments, grades, loading, readOnly, savingId, onChange, onSave }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage="No students enrolled yet"
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
