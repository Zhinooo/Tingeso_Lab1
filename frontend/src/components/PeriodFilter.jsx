import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { formatPeriod } from '../utils/periods'

function PeriodFilter({ periods, value, onChange, allLabel = 'Todos los períodos' }) {
  const isLoaded = periods.some((period) => String(period.academicPeriodId) === value)

  return (
    <TextField
      select
      size="small"
      label="Período académico"
      value={isLoaded ? value : ''}
      onChange={(event) => onChange(event.target.value)}
      sx={{ minWidth: 220 }}
    >
      {allLabel && <MenuItem value="">{allLabel}</MenuItem>}
      {periods.map((period) => (
        <MenuItem key={period.academicPeriodId} value={String(period.academicPeriodId)}>
          {formatPeriod(period)} · {period.academicPeriodStatus === 'OPEN' ? 'Abierto' : 'Cerrado'}
        </MenuItem>
      ))}
    </TextField>
  )
}

export default PeriodFilter
