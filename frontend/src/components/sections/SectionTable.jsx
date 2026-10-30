import DataTable from '../DataTable'
import SectionRow from './SectionRow'

const COLUMNS = [
  { label: 'Período' },
  { label: 'Ramo' },
  { label: 'Docente' },
  { label: 'Cupos', align: 'center' },
  { label: 'Horario' },
  { label: 'Acciones', align: 'right' },
]

function SectionTable({ sections, closedPeriodIds, loading, emptyHint, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={sections.length}
      loading={loading}
      emptyMessage="Aún no hay secciones"
      emptyHint={emptyHint}
    >
      {sections.map((section) => (
        <SectionRow
          key={section.sectionId}
          section={section}
          isClosed={closedPeriodIds.includes(section.academicPeriodId)}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default SectionTable
