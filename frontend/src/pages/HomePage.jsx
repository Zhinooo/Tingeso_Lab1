import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import useAuth from '../auth/useAuth'
import { formatRoles } from '../constants/roles'

function HomePage() {
  const { me } = useAuth()

  return (
    <Paper sx={{ p: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Bienvenido/a, {me.fullName}
      </Typography>
      <Typography color="text.secondary">
        Ingresaste como {formatRoles(me.roles)}. Usa el menú de la izquierda para comenzar.
      </Typography>
    </Paper>
  )
}

export default HomePage
