import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'

function PeriodFilter({ periods, value, onChange, allLabel = 'All periods' }) {
  return (
    <TextField
      select
      size="small"
      label="Academic period"
      value={value}
      onChange={(event) => onChange(event.target.value)}
      sx={{ minWidth: 220 }}
    >
      {allLabel && <MenuItem value="">{allLabel}</MenuItem>}
      {periods.map((period) => (
        <MenuItem key={period.academicPeriodId} value={String(period.academicPeriodId)}>
          {`${period.academicYear}-${period.academicSemester}`} ·{' '}
          {period.academicPeriodStatus === 'OPEN' ? 'Open' : 'Closed'}
        </MenuItem>
      ))}
    </TextField>
  )
}

export default PeriodFilter