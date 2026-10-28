import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/careers')
}

export default { getAll }