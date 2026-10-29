import { NavLink } from 'react-router-dom'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'

function NavItem({ to, label, icon: Icon, end }) {
  return (
    <ListItemButton
      component={NavLink}
      to={to}
      end={end}
      sx={{
        borderRadius: 2,
        mb: 0.25,
        minHeight: 40,
        color: 'inherit',
        '&:hover': { bgcolor: 'sidebar.hover' },
        '&.active, &.active:hover': { bgcolor: 'primary.main', color: 'common.white' },
        '&.active .MuiListItemText-primary': { fontWeight: 600 },
      }}
    >
      <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
        <Icon fontSize="small" />
      </ListItemIcon>
      <ListItemText primary={label} slotProps={{ primary: { sx: { fontWeight: 500, fontSize: 14 } } }} />
    </ListItemButton>
  )
}

export default NavItem