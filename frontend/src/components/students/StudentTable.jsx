import DataTable from '../DataTable'
import StudentRow from './StudentRow'

const COLUMNS = [
  { label: 'RUN' },
  { label: 'Full name' },
  { label: 'Email' },
  { label: 'Career' },
  { label: 'Study plan' },
  { label: 'Status' },
  { label: 'Actions', align: 'right' },
]

function StudentTable({ students, loading, onStatusChange, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={students.length}
      loading={loading}
      emptyMessage="No students yet"
      emptyHint="Students are assigned to the current study plan of their career."
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
