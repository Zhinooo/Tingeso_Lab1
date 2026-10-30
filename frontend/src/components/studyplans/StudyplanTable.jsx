import DataTable from '../DataTable'
import StudyplanRow from './StudyplanRow'

const COLUMNS = [{ label: 'Identifier' }, { label: 'Career' }, { label: 'Status' }, { label: 'Actions', align: 'right' }]

function StudyplanTable({ studyplans, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={studyplans.length}
      loading={loading}
      emptyMessage="No study plans yet"
      emptyHint="Create a study plan for a career to start adding courses."
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