import httpClient from '../http-common'

const getAll = () => {
  return httpClient.get('/api/enrollments')
}

const getStudentHistory = (studentId) => {
  return httpClient.get(`/api/enrollments/student/${studentId}/history`)
}

const getByAcademicPeriod = (academicPeriodId) => {
  return httpClient.get(`/api/enrollments/academic-period/${academicPeriodId}`)
}

const getMine = () => {
  return httpClient.get('/api/enrollments/me')
}

const getMyHistory = () => {
  return httpClient.get('/api/enrollments/me/history')
}

const getMyAvailable = (academicPeriodId) => {
  return httpClient.get('/api/enrollments/me/available', { params: { academicPeriodId } })
}

const getMySectionEnrollments = (sectionId) => {
  return httpClient.get(`/api/enrollments/me/section/${sectionId}`)
}

const create = (data) => {
  return httpClient.post('/api/enrollments', data)
}

const cancel = (id) => {
  return httpClient.delete(`/api/enrollments/${id}`)
}

const registerGrade = (id, data) => {
  return httpClient.put(`/api/enrollments/${id}/grade`, data)
}

export default {
  getAll,
  getStudentHistory,
  getByAcademicPeriod,
  getMine,
  getMyHistory,
  getMyAvailable,
  getMySectionEnrollments,
  create,
  cancel,
  registerGrade,
}
