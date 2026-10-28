import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/tuitions')
}

const getMine = () => {
  return httpClient.get('/api/tuitions/me')
}

const get = (id) => {
  return httpClient.get(`/api/tuitions/${id}`)
}

const getByStudent = (studentId) => {
  return httpClient.get(`/api/tuitions/student/${studentId}`)
}

const getByAcademicPeriod = (academicPeriodId) => {
  return httpClient.get(`/api/tuitions/academic-period/${academicPeriodId}`)
}

const create = (data) => {
  return httpClient.post('/api/tuitions', data)
}

const remove = (id) => {
  return httpClient.delete(`/api/tuitions/${id}`)
}

export default { getAll, getMine, get, getByStudent, getByAcademicPeriod, create, remove }
