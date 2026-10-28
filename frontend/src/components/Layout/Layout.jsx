import { useState } from 'react'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import AppBar from '@mui/material/AppBar'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Container from '@mui/material/Container'
import Divider from '@mui/material/Divider'
import Drawer from '@mui/material/Drawer'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import ListSubheader from '@mui/material/ListSubheader'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Toolbar from '@mui/material/Toolbar'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery'
import { useTheme } from '@mui/material/styles'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined'
import LogoutIcon from '@mui/icons-material/Logout'
import MenuIcon from '@mui/icons-material/Menu'
import SchoolIcon from '@mui/icons-material/School'
import useAuth from '../../auth/useAuth'
import { NAV, ROLE_LABEL } from './navConfig'

const DRAWER_WIDTH = 264
const MINI_WIDTH = 76

export default function Layout() {
  const { me, hasRole, logout } = useAuth()
  const { pathname } = useLocation()
  const theme = useTheme()
  const isSmUp = useMediaQuery(theme.breakpoints.up('sm'), { noSsr: true })
  const isMdUp = useMediaQuery(theme.breakpoints.up('md'), { noSsr: true })
  const [mobileOpen, setMobileOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(!isMdUp)
  const [menuAnchor, setMenuAnchor] = useState(null)
  const sidebarWidth = collapsed ? MINI_WIDTH : DRAWER_WIDTH
  const transition = theme.transitions.create('width', { duration: theme.transitions.duration.shorter })
  const fullName = me?.fullName || me?.email || 'User'
  const roles = me?.roles || []
  const roleLabel = roles.map((role) => ROLE_LABEL[role] || role).join(', ')
  const initials = fullName.trim().split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase()
  const isSelected = (to) => pathname === to || pathname.startsWith(`${to}/`)

  const renderSidebar = (mini) => (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#0f2747', color: '#c9d6ea' }}>
      <Toolbar component={RouterLink} to="/" onClick={() => setMobileOpen(false)} sx={{ gap: 1.5, textDecoration: 'none', justifyContent: mini ? 'center' : 'flex-start', px: mini ? '0 !important' : undefined }}>
        <Box sx={{ width: 34, height: 34, borderRadius: 2, bgcolor: 'primary.main', display: 'grid', placeItems: 'center', flexShrink: 0 }}>
          <SchoolIcon sx={{ color: '#fff', fontSize: 20 }} />
        </Box>
        {!mini && (
          <Box sx={{ whiteSpace: 'nowrap' }}>
            <Typography sx={{ color: '#fff', fontWeight: 700, lineHeight: 1.1 }}>SIGA IPT</Typography>
            <Typography variant="caption" sx={{ color: '#8ea3c4' }}>Academic management</Typography>
          </Box>
        )}
      </Toolbar>
      <Box sx={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', px: mini ? 1 : 1.5, pb: 2 }}>
        <List dense>
          <NavItem mini={mini} to="/" label="Home" icon={HomeOutlinedIcon} selected={pathname === '/'} onClick={() => setMobileOpen(false)} />
        </List>
        {NAV.filter((entry) => hasRole(entry.role)).flatMap((entry) =>
          entry.groups.map((group) => (
            <List key={`${entry.role}-${group.title}`} dense subheader={
              mini ? <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)', mx: 1, mb: 1 }} /> : (
                <ListSubheader disableSticky sx={{ bgcolor: 'transparent', color: '#7189ad', fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', lineHeight: '32px', px: 1.5, whiteSpace: 'nowrap' }}>
                  {group.title}
                </ListSubheader>
              )
            }>
              {group.items.map((item) => (
                <NavItem key={item.to} mini={mini} to={item.to} label={item.label} icon={item.icon} selected={isSelected(item.to)} onClick={() => setMobileOpen(false)} />
              ))}
            </List>
          )),
        )}
      </Box>
      {isSmUp && !mobileOpen && (
        <Box sx={{ p: 1, borderTop: '1px solid rgba(255,255,255,0.08)' }}>
          <Tooltip title={mini ? 'Expand sidebar' : ''} placement="right">
            <ListItemButton onClick={() => setCollapsed((value) => !value)} aria-label={mini ? 'Expand sidebar' : 'Collapse sidebar'} sx={{ borderRadius: 2, color: '#8ea3c4', justifyContent: mini ? 'center' : 'flex-start', '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' } }}>
              <ListItemIcon sx={{ minWidth: mini ? 0 : 36, color: 'inherit' }}>{mini ? <ChevronRightIcon fontSize="small" /> : <ChevronLeftIcon fontSize="small" />}</ListItemIcon>
              {!mini && <ListItemText primary="Collapse" slotProps={{ primary: { sx: { fontSize: 14 } } }} />}
            </ListItemButton>
          </Tooltip>
        </Box>
      )}
    </Box>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: '#f5f7fb' }}>
      <AppBar position="fixed" color="inherit" elevation={0} sx={{ width: { sm: `calc(100% - ${sidebarWidth}px)` }, ml: { sm: `${sidebarWidth}px` }, transition: theme.transitions.create(['width', 'margin']), bgcolor: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(8px)', borderBottom: '1px solid', borderColor: 'divider' }}>
        <Toolbar sx={{ gap: 1 }}>
          <IconButton edge="start" onClick={() => (isSmUp ? setCollapsed((value) => !value) : setMobileOpen(true))} aria-label={isSmUp ? 'Toggle sidebar' : 'Open menu'}>
            <MenuIcon />
          </IconButton>
          <Typography component={RouterLink} to="/" variant="h6" sx={{ color: 'primary.main', textDecoration: 'none', fontWeight: 800, display: { sm: 'none' } }}>SIGA IPT</Typography>
          <Box sx={{ flex: 1 }} />
          <Button onClick={(event) => setMenuAnchor(event.currentTarget)} color="inherit" aria-label="User menu" aria-controls={menuAnchor ? 'user-menu' : undefined} aria-haspopup="true" aria-expanded={Boolean(menuAnchor)} sx={{ gap: 1.25, px: 1, minWidth: 0 }}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main', fontSize: 14 }}>{initials}</Avatar>
            <Box sx={{ display: { xs: 'none', sm: 'block' }, textAlign: 'left' }}>
              <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>{fullName}</Typography>
              <Typography variant="caption" color="text.secondary">{me?.email}</Typography>
            </Box>
          </Button>
          <Menu id="user-menu" anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
            <MenuItem disabled>{fullName}</MenuItem>
            {roleLabel && <MenuItem disabled sx={{ fontSize: 12 }}>{roleLabel}</MenuItem>}
            <Divider />
            <MenuItem onClick={() => { setMenuAnchor(null); logout() }}>
              <ListItemIcon>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box component="nav" aria-label="Main navigation" sx={{ width: { sm: sidebarWidth }, flexShrink: { sm: 0 }, transition }}>
        <Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)} ModalProps={{ keepMounted: true }} sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}>
          {renderSidebar(false)}
        </Drawer>
        <Drawer variant="permanent" open sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { width: sidebarWidth, overflowX: 'hidden', transition } }}>
          {renderSidebar(collapsed)}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flex: 1, minWidth: 0 }}>
        <Toolbar />
        <Container maxWidth="xl" sx={{ py: { xs: 3, md: 4 }, px: { xs: 2, md: 4 } }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  )
}

function NavItem({ to, label, icon: Icon, selected, onClick, mini }) {
  const item = (
    <ListItemButton component={RouterLink} to={to} selected={selected} onClick={onClick} aria-label={label} aria-current={selected ? 'page' : undefined} sx={{
      borderRadius: 2, mb: 0.25, color: 'inherit', justifyContent: mini ? 'center' : 'flex-start', minHeight: 40,
      '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
      '&.Mui-selected, &.Mui-selected:hover': { bgcolor: 'primary.main', color: '#fff' },
    }}>
      <ListItemIcon sx={{ minWidth: mini ? 0 : 36, color: 'inherit' }}><Icon fontSize="small" /></ListItemIcon>
      {!mini && <ListItemText primary={label} slotProps={{ primary: { sx: { fontWeight: selected ? 600 : 500, fontSize: 14, whiteSpace: 'nowrap' } } }} />}
    </ListItemButton>
  )
  return mini ? <Tooltip title={label} placement="right">{item}</Tooltip> : item
}
