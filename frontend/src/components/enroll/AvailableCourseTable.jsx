import DataTable from '../DataTable'
import AvailableCourseRow from './AvailableCourseRow'

const COLUMNS = [
  { label: '' },
  { label: 'Sem.' },
  { label: 'Course' },
  { label: 'Teacher' },
  { label: 'Schedule' },
  { label: 'Seats' },
]

function AvailableCourseTable({ courses, selected, loading, onToggle }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={courses.length}
      loading={loading}
      emptyMessage="No courses available for this period"
      emptyHint="Only courses of your plan with an open section and approved prerequisites are listed."
    >
      {courses.map((course) => (
        <AvailableCourseRow
          key={course.courseId}
          course={course}
          checked={selected.includes(course.courseId)}
          onToggle={onToggle}
        />
      ))}
    </DataTable>
  )
}

export default AvailableCourseTable
