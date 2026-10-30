import Box from '@mui/material/Box'
import Drawer from '@mui/material/Drawer'
import List from '@mui/material/List'
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined'
import useAuth from '../../auth/useAuth'
import NavItem from './NavItem'
import SidebarBrand from './SidebarBrand'
import { NAV } from './navConfig'

function Sidebar({ width }) {
  const { hasRole } = useAuth()
  const items = NAV.filter((entry) => hasRole(entry.role)).flatMap((entry) => entry.items)

  return (
    <Drawer
      variant="permanent"
      sx={{
        width,
        flexShrink: 0,
        '& .MuiDrawer-paper': { width, bgcolor: 'sidebar.main', color: 'sidebar.text' },
      }}
    >
      <SidebarBrand />
      <Box sx={{ px: 1.5, pb: 2 }}>
        <List dense>
          <NavItem to="/" label="Inicio" icon={HomeOutlinedIcon} end />
          {items.map((item) => (
            <NavItem key={item.to} to={item.to} label={item.label} icon={item.icon} />
          ))}
        </List>
      </Box>
    </Drawer>
  )
}

export default Sidebar
