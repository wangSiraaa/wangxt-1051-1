import request from '@/utils/request'

export function getPenaltyClues() {
  return request.get('/penalty-clues')
}

export function getPenaltyCluesByStatus(status) {
  return request.get(`/penalty-clues/status/${status}`)
}

export function getPenaltyCluesByPermit(permitId) {
  return request.get(`/penalty-clues/permit/${permitId}`)
}

export function getPenaltyCluesByEnterprise(enterpriseId) {
  return request.get(`/penalty-clues/enterprise/${enterpriseId}`)
}

export function getPenaltyClueById(id) {
  return request.get(`/penalty-clues/${id}`)
}

export function submitPenaltyClueForReview(id) {
  return request.post(`/penalty-clues/${id}/submit`)
}

export function reviewPenaltyClue(id, data) {
  return request.post(`/penalty-clues/${id}/review`, data)
}

export function processPenaltyClue(id, handleResult) {
  return request.post(`/penalty-clues/${id}/process`, { handleResult })
}
