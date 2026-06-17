import request from '@/utils/request'

export function getDeclarations(params) {
  return request.get('/declarations', { params })
}

export function getDeclarationById(id) {
  return request.get(`/declarations/${id}`)
}

export function getDeclarationByNo(declarationNo) {
  return request.get(`/declarations/no/${declarationNo}`)
}

export function createDeclaration(data) {
  return request.post('/declarations', data)
}

export function submitDeclaration(id) {
  return request.post(`/declarations/${id}/submit`)
}

export function verifyDeclaration(id, data) {
  return request.post(`/declarations/${id}/verify`, data)
}

export function cancelDeclaration(id) {
  return request.post(`/declarations/${id}/cancel`)
}
