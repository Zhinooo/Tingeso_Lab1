import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'

function PrerequisiteChips({ codes }) {
  if (codes.length === 0) {
    return (
      <Typography variant="body2" color="text.disabled">
        None
      </Typography>
    )
  }

  return (
    <Stack direction="row" sx={{ gap: 0.5, flexWrap: 'wrap' }}>
      {codes.map((code) => (
        <Chip key={code} label={code} size="small" variant="outlined" />
      ))}
    </Stack>
  )
}

export default PrerequisiteChips
