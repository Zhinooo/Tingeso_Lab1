import { Link as RouterLink } from 'react-router-dom'
import Box from '@mui/material/Box'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import SchoolIcon from '@mui/icons-material/School'

function SidebarBrand() {
  return (
    <Toolbar component={RouterLink} to="/" sx={{ gap: 1.5, textDecoration: 'none' }}>
      <Box
        sx={{
          width: 34,
          height: 34,
          borderRadius: 2,
          bgcolor: 'primary.main',
          display: 'grid',
          placeItems: 'center',
        }}
      >
        <SchoolIcon sx={{ color: 'common.white', fontSize: 20 }} />
      </Box>
      <Box>
        <Typography sx={{ color: 'common.white', fontWeight: 700, lineHeight: 1.1 }}>SIGA IPT</Typography>
        <Typography variant="caption" sx={{ color: 'sidebar.muted' }}>
          Gestión académica
        </Typography>
      </Box>
    </Toolbar>
  )
}

export default SidebarBrand