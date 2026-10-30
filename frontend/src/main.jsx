import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import CssBaseline from '@mui/material/CssBaseline'
import { ThemeProvider } from '@mui/material/styles'
import App from './App.jsx'
import AuthProvider from './auth/AuthProvider'
import keycloak from './auth/keycloak'
import theme from './theme'

const root = createRoot(document.getElementById('root'))

keycloak
  .init({
    onLoad: 'login-required',
    pkceMethod: 'S256',
    checkLoginIframe: false,
  })
  .then(() => {
    root.render(
      <StrictMode>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <AuthProvider>
            <App />
          </AuthProvider>
        </ThemeProvider>
      </StrictMode>,
    )
  })
  .catch((error) => {
    console.error('Keycloak init failed', error)
    root.render(<p>No se pudo conectar con el servidor de inicio de sesión.</p>)
  })
