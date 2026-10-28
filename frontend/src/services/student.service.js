import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/students')
}

const getMine = () => {
  return httpClient.get('/api/students/me')
}

const get = (id) => {
  return httpClient.get(`/api/students/${id}`)
}

const create = (data) => {
  return httpClient.post('/api/students', data)
}

const update = (id, data) => {
  return httpClient.put(`/api/students/${id}`, data)
}

const changeCareer = (id, data) => {
  return httpClient.put(`/api/students/${id}/career`, data)
}

const changeStatus = (id, status) => {
  return httpClient.put(`/api/students/${id}/status`, null, { params: { status } })
}

const remove = (id) => {
  return httpClient.delete(`/api/students/${id}`)
}

export default { getAll, getMine, get, create, update, changeCareer, changeStatus, remove }
