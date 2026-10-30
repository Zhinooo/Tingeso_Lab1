import { useEffect, useState } from 'react'
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

  if (error) return <p>{error}</p>
  if (!me) return <p>Cargando...</p>

  const hasRole = (role) => me.roles.includes(role)

  const logout = () => {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  return <AuthContext value={{ me, hasRole, logout }}>{children}</AuthContext>
}

export default AuthProvider