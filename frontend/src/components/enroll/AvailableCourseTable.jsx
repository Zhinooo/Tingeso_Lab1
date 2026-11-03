import DataTable from '../DataTable'
import AvailableCourseRow from './AvailableCourseRow'

const COLUMNS = [
  { label: '' },
  { label: 'Sem.' },
  { label: 'Ramo' },
  { label: 'Docente' },
  { label: 'Horario' },
  { label: 'Cupos' },
]

function AvailableCourseTable({ courses, selected, conflictsByCourse, loading, onToggle }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={courses.length}
      loading={loading}
      emptyMessage="No hay ramos disponibles para este período"
      emptyHint="Solo aparecen ramos de tu plan con sección abierta y prerrequisitos aprobados."
    >
      {courses.map((course) => (
        <AvailableCourseRow
          key={course.courseId}
          course={course}
          checked={selected.includes(course.courseId)}
          conflictsWith={conflictsByCourse[course.courseId] ?? []}
          onToggle={onToggle}
        />
      ))}
    </DataTable>
  )
}

export default AvailableCourseTable
