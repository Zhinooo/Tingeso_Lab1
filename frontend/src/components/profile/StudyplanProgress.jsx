import Box from '@mui/material/Box'
import LinearProgress from '@mui/material/LinearProgress'
import Typography from '@mui/material/Typography'

function StudyplanProgress({ progress }) {
  return (
    <Box sx={{ minWidth: 320 }}>
      <Typography variant="body2" sx={{ fontWeight: 600 }}>
        {progress.approvedCount} de {progress.totalCount} ramos aprobados ({progress.percent}%)
      </Typography>
      <LinearProgress variant="determinate" value={progress.percent} sx={{ my: 0.5, height: 8, borderRadius: 1 }} />
      <Typography variant="caption" color="text.secondary">
        {progress.approvedSct} de {progress.totalSct} SCT
      </Typography>
    </Box>
  )
}

export default StudyplanProgress
