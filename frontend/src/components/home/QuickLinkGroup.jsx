import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Typography from '@mui/material/Typography'
import QuickLinkCard from './QuickLinkCard'

function QuickLinkGroup({ title, items }) {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="overline" color="text.secondary" sx={{ fontWeight: 700 }}>
        {title}
      </Typography>
      <Grid container spacing={2} sx={{ mt: 0.5 }}>
        {items.map((item) => (
          <Grid key={item.to} size={4}>
            <QuickLinkCard to={item.to} label={item.label} description={item.description} icon={item.icon} />
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}

export default QuickLinkGroup
