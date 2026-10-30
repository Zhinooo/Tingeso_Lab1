import { Link as RouterLink } from 'react-router-dom'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'

function QuickLinkCard({ to, label, description, icon: Icon }) {
  return (
    <Card
      variant="outlined"
      sx={{
        height: '100%',
        transition: 'border-color .15s, transform .15s',
        '&:hover': { borderColor: 'primary.main', transform: 'translateY(-2px)' },
      }}
    >
      <CardActionArea component={RouterLink} to={to} sx={{ height: '100%' }}>
        <CardContent sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Box
            sx={{
              width: 44,
              height: 44,
              borderRadius: 2,
              bgcolor: 'action.selected',
              color: 'primary.main',
              display: 'grid',
              placeItems: 'center',
              flexShrink: 0,
            }}
          >
            <Icon />
          </Box>
          <Box sx={{ flex: 1 }}>
            <Typography sx={{ fontWeight: 600 }}>{label}</Typography>
            <Typography variant="body2" color="text.secondary">
              {description}
            </Typography>
          </Box>
          <ArrowForwardIcon fontSize="small" color="action" />
        </CardContent>
      </CardActionArea>
    </Card>
  )
}

export default QuickLinkCard
