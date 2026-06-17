import request from '@/utils/request'

export function getApplications(params) {
  return request.get('/applications', { params })
}

export function getApplicationById(id) {
  return request.get(`/applications/${id}`)
}

export function getApplicationByNo(applicationNo) {
  return request.get(`/applications/no/${applicationNo}`)
}

export function createApplication(data) {
  return request.post('/applications', data)
}

export function submitApplication(id) {
  return request.post(`/applications/${id}/submit`)
}

export function reviewApplication(id, data) {
  return request.post(`/applications/${id}/review`, data)
}

export function changeApplication(id, data) {
  return request.put(`/applications/${id}/change`, data)
}

export function suspendApplication(id, reason) {
  return request.post(`/applications/${id}/suspend`, { reason })
}

export function resumeApplication(id) {
  return request.post(`/applications/${id}/resume`)
}

export function getHistoricalViolations(enterpriseId) {
  return request.get(`/applications/${enterpriseId}/violations`)
}
