import { Link as RouterLink } from 'react-router-dom'
import Button from '@mui/material/Button'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'

function SummaryCard({ label, value, detail, linkLabel, to }) {
  return (
    <Paper sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{ textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}
      >
        {label}
      </Typography>
      <Typography variant="h4" sx={{ fontWeight: 700, my: 1 }}>
        {value}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ flex: 1 }}>
        {detail}
      </Typography>
      {to && (
        <Button component={RouterLink} to={to} size="small" sx={{ mt: 2, alignSelf: 'flex-start', ml: -0.5 }}>
          {linkLabel}
        </Button>
      )}
    </Paper>
  )
}

export default SummaryCard
