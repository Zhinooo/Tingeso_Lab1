import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import AuthContext from './AuthContext'
import keycloak from './keycloak'
import meService from '../services/me.service'

function AuthProvider({ children }) {
  const [me, setMe] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    meService
      .getMe()
      .then((response) => setMe(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const logout = () => {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  if (error) {
    return (
      <Box sx={{ p: 4 }}>
        <Alert
          severity="error"
          action={
            <>
              <Button color="inherit" size="small" onClick={() => window.location.reload()}>
                Reintentar
              </Button>
              <Button color="inherit" size="small" onClick={logout}>
                Cerrar sesión
              </Button>
            </>
          }
        >
          No se pudo cargar tu cuenta: {error}
        </Alert>
      </Box>
    )
  }
  if (!me) return <p>Cargando...</p>

  const hasRole = (role) => me.roles.includes(role)

  return <AuthContext value={{ me, hasRole, logout }}>{children}</AuthContext>
}

export default AuthProvider