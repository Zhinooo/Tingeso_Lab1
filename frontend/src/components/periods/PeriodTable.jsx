import { comparePeriodsDesc } from '../../utils/periods'
import DataTable from '../DataTable'
import PeriodRow from './PeriodRow'

const COLUMNS = [{ label: 'Period' }, { label: 'Status' }, { label: 'Actions', align: 'right' }]

function PeriodTable({ periods, loading, onClosePeriod, onDelete }) {
  const sortedPeriods = [...periods].sort(comparePeriodsDesc)

  return (
    <DataTable
      columns={COLUMNS}
      rowCount={periods.length}
      loading={loading}
      emptyMessage="No academic periods yet"
      emptyHint="Create a period to start offering sections."
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
