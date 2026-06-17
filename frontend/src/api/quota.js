import request from '@/utils/request'

export function getAllQuotas() {
  return request.get('/quota')
}

export function getQuotasByYear(year) {
  return request.get(`/quota/year/${year}`)
}

export function getQuota(year, riverSectionId, riverSectionName) {
  return request.get(`/quota/${year}/${riverSectionId}`, { params: { riverSectionName } })
}

export function getRemainingQuota(year, riverSectionId) {
  return request.get(`/quota/remaining/${year}/${riverSectionId}`)
}

export function getForbiddenPeriods() {
  return request.get('/quota/forbidden-periods')
}

export function checkForbiddenPeriod(date) {
  return request.get('/quota/check-forbidden', { params: { date } })
}

export function checkForbiddenOverlap(startDate, endDate) {
  return request.get('/quota/check-overlap', { params: { startDate, endDate } })
}
