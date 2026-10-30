import DataTable from '../DataTable'
import StudyplanRow from './StudyplanRow'

const COLUMNS = [{ label: 'Identificador' }, { label: 'Carrera' }, { label: 'Estado' }, { label: 'Acciones', align: 'right' }]

function StudyplanTable({ studyplans, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={studyplans.length}
      loading={loading}
      emptyMessage="Aún no hay planes de estudio"
      emptyHint="Crea un plan de estudio para una carrera y luego agrega sus ramos."
    >
      {studyplans.map((studyplan) => (
        <StudyplanRow
          key={studyplan.studyplanId}
          studyplan={studyplan}
          onToggleStatus={onToggleStatus}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default StudyplanTable