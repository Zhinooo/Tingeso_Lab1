import { useState } from 'react'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Divider from '@mui/material/Divider'
import ListItemIcon from '@mui/material/ListItemIcon'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Typography from '@mui/material/Typography'
import LogoutIcon from '@mui/icons-material/Logout'
import useAuth from '../../auth/useAuth'
import { getInitials } from '../../utils/names'
import { ROLE_LABEL } from './navConfig'

function UserMenu() {
  const { me, logout } = useAuth()
  const [menuAnchor, setMenuAnchor] = useState(null)

  const roleLabel = me.roles.map((role) => ROLE_LABEL[role] || role).join(', ')
  const initials = getInitials(me.fullName)

  const handleLogout = () => {
    setMenuAnchor(null)
    logout()
  }

  return (
    <>
      <Button
        onClick={(event) => setMenuAnchor(event.currentTarget)}
        color="inherit"
        aria-label="User menu"
        aria-controls={menuAnchor ? 'user-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={Boolean(menuAnchor)}
        sx={{ gap: 1.25, px: 1, minWidth: 0 }}
      >
        <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main', fontSize: 14 }}>{initials}</Avatar>
        <Box sx={{ textAlign: 'left' }}>
          <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
            {me.fullName}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {me.email}
          </Typography>
        </Box>
      </Button>
      <Menu
        id="user-menu"
        anchorEl={menuAnchor}
        open={Boolean(menuAnchor)}
        onClose={() => setMenuAnchor(null)}
      >
        <MenuItem disabled>{me.fullName}</MenuItem>
        {roleLabel && (
          <MenuItem disabled sx={{ fontSize: 12 }}>
            {roleLabel}
          </MenuItem>
        )}
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" />
          </ListItemIcon>
          Logout
        </MenuItem>
      </Menu>
    </>
  )
}

export default UserMenu