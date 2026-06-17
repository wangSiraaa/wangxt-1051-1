import request from '@/utils/request'

export function getDetections(params) {
  return request.get('/detentions', { params })
}

export function getDetentionById(id) {
  return request.get(`/detentions/${id}`)
}

export function getDetentionByNo(detentionNo) {
  return request.get(`/detentions/no/${detentionNo}`)
}

export function createDetention(data) {
  return request.post('/detentions', data)
}

export function startRectification(id, requirement) {
  return request.post(`/detentions/${id}/start-rectification`, { requirement })
}

export function releaseDetention(id, reason) {
  return request.post(`/detentions/${id}/release`, { reason })
}

export function confirmViolation(id, reason) {
  return request.post(`/detentions/${id}/confirm-violation`, { reason })
}
