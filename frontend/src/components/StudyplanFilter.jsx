import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'

function StudyplanFilter({ studyplans, value, onChange, allLabel = 'All study plans' }) {
  const isLoaded = studyplans.some((studyplan) => String(studyplan.studyplanId) === value)

  return (
    <TextField
      select
      size="small"
      label="Study plan"
      value={isLoaded ? value : ''}
      onChange={(event) => onChange(event.target.value)}
      sx={{ minWidth: 240 }}
    >
      {allLabel && <MenuItem value="">{allLabel}</MenuItem>}
      {studyplans.map((studyplan) => (
        <MenuItem key={studyplan.studyplanId} value={String(studyplan.studyplanId)}>
          {studyplan.careerCode} · {studyplan.studyplanIdentifier}
        </MenuItem>
      ))}
    </TextField>
  )
}

export default StudyplanFilter
