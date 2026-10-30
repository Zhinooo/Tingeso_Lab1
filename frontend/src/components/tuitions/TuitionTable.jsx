import DataTable from '../DataTable'
import TuitionRow from './TuitionRow'

const COLUMNS = [
  { label: 'Student RUN' },
  { label: 'Student' },
  { label: 'Period' },
  { label: 'Date' },
  { label: 'Actions', align: 'right' },
]

function TuitionTable({ tuitions, studentNames, loading, emptyHint, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={tuitions.length}
      loading={loading}
      emptyMessage="No tuitions yet"
      emptyHint={emptyHint}
    >
      {tuitions.map((tuition) => (
        <TuitionRow
          key={tuition.tuitionId}
          tuition={tuition}
          studentName={studentNames[tuition.studentId]}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default TuitionTable
