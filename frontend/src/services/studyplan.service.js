import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/studyplans')
}

const get = (id) => {
  return httpClient.get(`/api/studyplans/${id}`)
}

const create = (data) => {
  return httpClient.post('/api/studyplans', data)
}

const update = (id, data) => {
  return httpClient.put(`/api/studyplans/${id}`, data)
}

const changeStatus = (id, status) => {
  return httpClient.put(`/api/studyplans/${id}/status`, null, { params: { status } })
}

const remove = (id) => {
  return httpClient.delete(`/api/studyplans/${id}`)
}

export default { getAll, get, create, update, changeStatus, remove }
