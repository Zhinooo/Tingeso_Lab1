import axios from 'axios'
import keycloak from './auth/keycloak'

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

httpClient.interceptors.request.use(async (config) => {
  try {
    await keycloak.updateToken(30)
  } catch {
    keycloak.login()
  }
  config.headers.Authorization = `Bearer ${keycloak.token}`
  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.login()
    }
    const message = error.response?.data?.message ?? 'No se pudo conectar con el servidor'
    return Promise.reject(new Error(message))
  },
)

export default httpClient