import DataTable from '../DataTable'
import CourseRow from './CourseRow'

const COLUMNS = [
  { label: 'Sem.' },
  { label: 'Code' },
  { label: 'Name' },
  { label: 'TEL' },
  { label: 'SCT' },
  { label: 'Plan' },
  { label: 'Prerequisites' },
  { label: 'Actions', align: 'right' },
]

const compareCourses = (a, b) =>
  a.studyplanIdentifier.localeCompare(b.studyplanIdentifier) ||
  a.semester - b.semester ||
  a.courseCode.localeCompare(b.courseCode)

function CourseTable({ courses, loading, emptyHint, onDelete }) {
  const sortedCourses = [...courses].sort(compareCourses)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={courses.length}
      loading={loading}
      emptyMessage="No courses yet"
      emptyHint={emptyHint}
    >
      {sortedCourses.map((course) => (
        <CourseRow key={course.courseId} course={course} onDelete={onDelete} />
      ))}
    </DataTable>
  )
}

export default CourseTable
