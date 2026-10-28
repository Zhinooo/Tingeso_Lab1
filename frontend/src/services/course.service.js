import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/courses')
}

const get = (id) => {
  return httpClient.get(`/api/courses/${id}`)
}

const getByStudyplan = (studyplanId) => {
  return httpClient.get(`/api/courses/studyplan/${studyplanId}`)
}

const create = (data) => {
  return httpClient.post('/api/courses', data)
}

const update = (id, data) => {
  return httpClient.put(`/api/courses/${id}`, data)
}

const remove = (id) => {
  return httpClient.delete(`/api/courses/${id}`)
}

export default { getAll, get, getByStudyplan, create, update, remove }
