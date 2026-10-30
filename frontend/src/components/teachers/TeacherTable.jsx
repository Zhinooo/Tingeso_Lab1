import DataTable from '../DataTable'
import TeacherRow from './TeacherRow'

const COLUMNS = [
  { label: 'RUN' },
  { label: 'Full name' },
  { label: 'Email' },
  { label: 'Professional title' },
  { label: 'Degree' },
  { label: 'Status' },
  { label: 'Actions', align: 'right' },
]

function TeacherTable({ teachers, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={teachers.length}
      loading={loading}
      emptyMessage="No teachers yet"
      emptyHint="Add a teacher to be able to assign sections."
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
