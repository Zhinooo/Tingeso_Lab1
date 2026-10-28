import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/teachers')
}

const getMine = () => {
  return httpClient.get('/api/teachers/me')
}

const get = (id) => {
  return httpClient.get(`/api/teachers/${id}`)
}

const create = (data) => {
  return httpClient.post('/api/teachers', data)
}

const update = (id, data) => {
  return httpClient.put(`/api/teachers/${id}`, data)
}

const changeStatus = (id, status) => {
  return httpClient.put(`/api/teachers/${id}/status`, null, { params: { status } })
}

const remove = (id) => {
  return httpClient.delete(`/api/teachers/${id}`)
}

export default { getAll, getMine, get, create, update, changeStatus, remove }
