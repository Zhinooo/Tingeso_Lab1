import { comparePeriodsDesc } from '../../utils/periods'
import DataTable from '../DataTable'
import MyEnrollmentRow from './MyEnrollmentRow'

const COLUMNS = [
  { label: 'Period' },
  { label: 'Code' },
  { label: 'Course' },
  { label: 'Date' },
  { label: 'Grade', align: 'center' },
  { label: 'Result' },
  { label: 'Actions', align: 'right' },
]

function MyEnrollmentTable({ enrollments, loading, onCancel }) {
  const sortedEnrollments = [...enrollments].sort(comparePeriodsDesc)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={enrollments.length}
      loading={loading}
      emptyMessage="You have no enrollments yet"
      emptyHint="Enroll courses in an open period where you have a tuition."
    >
      {sortedEnrollments.map((enrollment) => (
        <MyEnrollmentRow key={enrollment.enrollmentId} enrollment={enrollment} onCancel={onCancel} />
      ))}
    </DataTable>
  )
}

export default MyEnrollmentTable
