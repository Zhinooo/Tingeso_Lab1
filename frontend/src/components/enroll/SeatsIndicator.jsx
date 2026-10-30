import LinearProgress from '@mui/material/LinearProgress'
import Typography from '@mui/material/Typography'

function SeatsIndicator({ available, capacity }) {
  const isFull = available <= 0
  const taken = ((capacity - available) / capacity) * 100

  return (
    <>
      <Typography variant="body2" sx={{ fontWeight: 600, color: isFull ? 'error.main' : 'text.primary' }}>
        {isFull ? 'Full' : `${available} / ${capacity}`}
      </Typography>
      <LinearProgress
        variant="determinate"
        value={taken}
        color={isFull ? 'error' : 'primary'}
        sx={{ mt: 0.5, borderRadius: 1 }}
      />
    </>
  )
}

export default SeatsIndicator
