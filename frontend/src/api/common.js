import request from '@/utils/request'

export function login(data) {
  return request.post('/common/login', data)
}

export function getUsers() {
  return request.get('/common/users')
}

export function getUsersByRole(role) {
  return request.get(`/common/users/role/${role}`)
}

export function getUserById(id) {
  return request.get(`/common/users/${id}`)
}

export function getRiverSections() {
  return request.get('/common/river-sections')
}

export function getRiverSectionById(id) {
  return request.get(`/common/river-sections/${id}`)
}

export function getVessels(ownerId) {
  const params = ownerId ? { ownerId } : {}
  return request.get('/common/vessels', { params })
}

export function getVesselById(id) {
  return request.get(`/common/vessels/${id}`)
}

export function getAuditTimeline(businessType, businessId) {
  return request.get('/common/audit-timeline', { params: { businessType, businessId } })
}

export function getLatestAuditTimeline(limit = 20) {
  return request.get('/common/audit-timeline/latest', { params: { limit } })
}

export function getEnums() {
  return request.get('/common/enums')
}
