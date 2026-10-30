import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { formatPeriod } from '../utils/periods'

function PeriodFilter({ periods, value, onChange, allLabel = 'All periods' }) {
  const isLoaded = periods.some((period) => String(period.academicPeriodId) === value)

  return (
    <TextField
      select
      size="small"
      label="Academic period"
      value={isLoaded ? value : ''}
      onChange={(event) => onChange(event.target.value)}
      sx={{ minWidth: 220 }}
    >
      {allLabel && <MenuItem value="">{allLabel}</MenuItem>}
      {periods.map((period) => (
        <MenuItem key={period.academicPeriodId} value={String(period.academicPeriodId)}>
          {formatPeriod(period)} · {period.academicPeriodStatus === 'OPEN' ? 'Open' : 'Closed'}
        </MenuItem>
      ))}
    </TextField>
  )
}

export default PeriodFilter
