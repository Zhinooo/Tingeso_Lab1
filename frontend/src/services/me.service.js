import httpClient from '../http-common'

const getMe = () => {
  return httpClient.get('/api/me')
}

export default { getMe }