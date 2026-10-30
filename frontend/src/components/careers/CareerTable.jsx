import DataTable from '../DataTable'
import CareerRow from './CareerRow'

const COLUMNS = [
  { label: 'Código' },
  { label: 'Nombre' },
  { label: 'Descripción' },
  { label: 'Estado' },
  { label: 'Acciones', align: 'right' },
]

function CareerTable({ careers, loading, onToggleStatus, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={careers.length}
      loading={loading}
      emptyMessage="Aún no hay carreras"
      emptyHint="Crea la primera carrera para empezar a armar planes de estudio."
    >
      {careers.map((career) => (
        <CareerRow key={career.careerId} career={career} onToggleStatus={onToggleStatus} onDelete={onDelete} />
      ))}
    </DataTable>
  )
}

export default CareerTable