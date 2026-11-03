import { NO_MATCH_HINT, NO_MATCH_MESSAGE } from '../../constants/search'
import DataTable from '../DataTable'
import CourseRow from './CourseRow'

const COLUMNS = [
  { label: 'Sem.' },
  { label: 'Código' },
  { label: 'Nombre' },
  { label: 'TEL' },
  { label: 'SCT' },
  { label: 'Plan' },
  { label: 'Prerrequisitos' },
  { label: 'Acciones', align: 'right' },
]

const compareCourses = (a, b) =>
  a.studyplanIdentifier.localeCompare(b.studyplanIdentifier) ||
  a.semester - b.semester ||
  a.courseCode.localeCompare(b.courseCode)

function CourseTable({ courses, loading, isFiltered, emptyHint, onDelete }) {
  const sortedCourses = [...courses].sort(compareCourses)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={courses.length}
      loading={loading}
      emptyMessage={isFiltered ? NO_MATCH_MESSAGE : 'Aún no hay ramos'}
      emptyHint={isFiltered ? NO_MATCH_HINT : emptyHint}
    >
      {sortedCourses.map((course) => (
        <CourseRow key={course.courseId} course={course} onDelete={onDelete} />
      ))}
    </DataTable>
  )
}

export default CourseTable
