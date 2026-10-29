import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'

function Stat({ label, value }) {
  return (
    <Box>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}
      >
        {label}
      </Typography>
      <Typography sx={{ fontWeight: 600 }}>{value}</Typography>
    </Box>
  )
}

export default Stat