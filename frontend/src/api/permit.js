import request from '@/utils/request'

export function getPermits(params) {
  return request.get('/permits', { params })
}

export function getPermitById(id) {
  return request.get(`/permits/${id}`)
}

export function getPermitByNo(permitNo) {
  return request.get(`/permits/no/${permitNo}`)
}

export function getPermitByApplicationId(applicationId) {
  return request.get(`/permits/application/${applicationId}`)
}

export function checkPermitExpiry() {
  return request.post('/permits/check-expiry')
}
