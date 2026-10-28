import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import keycloak from './auth/keycloak'

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
        <App />
      </StrictMode>,
    )
  })
  .catch((error) => {
    console.error('Keycloak init failed', error)
    root.render(<p>Could not connect to the login server.</p>)
  })
