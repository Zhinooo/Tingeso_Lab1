import { comparePeriodsDesc } from '../../utils/periods'
import DataTable from '../DataTable'
import PeriodRow from './PeriodRow'

const COLUMNS = [{ label: 'Período' }, { label: 'Estado' }, { label: 'Acciones', align: 'right' }]

function PeriodTable({ periods, loading, onClosePeriod, onDelete }) {
  const sortedPeriods = [...periods].sort(comparePeriodsDesc)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={periods.length}
      loading={loading}
      emptyMessage="Aún no hay períodos académicos"
      emptyHint="Crea un período para empezar a ofrecer secciones."
    >
      {sortedPeriods.map((period) => (
        <PeriodRow
          key={period.academicPeriodId}
          period={period}
          onClosePeriod={onClosePeriod}
          onDelete={onDelete}
        />
      ))}
    </DataTable>
  )
}

export default PeriodTable
