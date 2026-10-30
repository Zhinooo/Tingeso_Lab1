import DataTable from '../DataTable'
import EnrollmentRow from './EnrollmentRow'

const COLUMNS = [
  { label: 'Student RUN' },
  { label: 'Period' },
  { label: 'Code' },
  { label: 'Course' },
  { label: 'Date' },
  { label: 'Grade', align: 'center' },
  { label: 'Result' },
]

function EnrollmentTable({ enrollments, loading }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage="No enrollments yet"
      emptyHint="Students enroll in courses from their own portal."
    >
      {enrollments.map((enrollment) => (
        <EnrollmentRow key={enrollment.enrollmentId} enrollment={enrollment} />
      ))}
    </DataTable>
  )
}

export default EnrollmentTable
