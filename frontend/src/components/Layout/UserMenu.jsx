import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import LogoutIcon from '@mui/icons-material/Logout'
import useAuth from '../../auth/useAuth'
import { formatRoles } from '../../constants/roles'

function UserMenu() {
  const { me, logout } = useAuth()

  return (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
      <Box sx={{ textAlign: 'right' }}>
        <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2 }}>
          {me.fullName}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {formatRoles(me.roles)}
        </Typography>
      </Box>
      <Button variant="outlined" size="small" startIcon={<LogoutIcon />} onClick={logout}>
       Cerrar sesión
      </Button>
    </Stack>
  )
}

export default UserMenu
