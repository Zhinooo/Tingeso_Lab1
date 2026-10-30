import DataTable from '../DataTable'
import TuitionRow from './TuitionRow'

const COLUMNS = [
  { label: 'RUN estudiante' },
  { label: 'Estudiante' },
  { label: 'Período' },
  { label: 'Fecha' },
  { label: 'Acciones', align: 'right' },
]

function TuitionTable({ tuitions, studentNames, loading, emptyHint, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={tuitions.length}
      loading={loading}
      emptyMessage="Aún no hay matrículas"
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
