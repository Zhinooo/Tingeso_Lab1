import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/sections')
}

const getMine = () => {
  return httpClient.get('/api/sections/me')
}

const get = (id) => {
  return httpClient.get(`/api/sections/${id}`)
}

const getByAcademicPeriod = (academicPeriodId) => {
  return httpClient.get(`/api/sections/academic-period/${academicPeriodId}`)
}

const create = (data) => {
  return httpClient.post('/api/sections', data)
}

const update = (id, data) => {
  return httpClient.put(`/api/sections/${id}`, data)
}

const remove = (id) => {
  return httpClient.delete(`/api/sections/${id}`)
}

export default { getAll, getMine, get, getByAcademicPeriod, create, update, remove }
