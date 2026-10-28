import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import AuthProvider from './auth/AuthProvider'
import keycloak from './auth/keycloak'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'

const root = createRoot(document.getElementById('root'))
const theme = createTheme({
  palette: { primary: { main: '#1565c0' } },
})

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
      </StrictMode>
    )
  })
  .catch((error) => {
    console.error('Keycloak init failed', error)
    root.render(<p>Could not connect to the login server.</p>)
  })
