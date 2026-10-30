import DataTable from '../DataTable'
import SectionRow from './SectionRow'

const COLUMNS = [
  { label: 'Period' },
  { label: 'Course' },
  { label: 'Teacher' },
  { label: 'Capacity', align: 'center' },
  { label: 'Schedule' },
  { label: 'Actions', align: 'right' },
]

function SectionTable({ sections, loading, emptyHint, onDelete }) {
  return (
    <DataTable
      columns={COLUMNS}
      rowCount={sections.length}
      loading={loading}
      emptyMessage="No sections yet"
      emptyHint={emptyHint}
    >
      {sections.map((section) => (
        <SectionRow key={section.sectionId} section={section} onDelete={onDelete} />
      ))}
    </DataTable>
  )
}

export default SectionTable
