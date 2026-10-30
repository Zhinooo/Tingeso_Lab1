import DataTable from '../DataTable'
import CareerRow from './CareerRow'

const COLUMNS = [
  { label: 'Code' },
  { label: 'Name' },
  { label: 'Description' },
  { label: 'Status' },
  { label: 'Actions', align: 'right' },
]

function CareerTable({ careers, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={careers.length}
      loading={loading}
      emptyMessage="No careers yet"
      emptyHint="Create the first career to start building study plans."
    >
      {careers.map((career) => (
        <CareerRow key={career.careerId} career={career} onToggleStatus={onToggleStatus} onDelete={onDelete} />
      ))}
    </DataTable>
  )
}

export default CareerTable