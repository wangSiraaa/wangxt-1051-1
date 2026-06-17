import request from '@/utils/request'

export function getInspections() {
  return request.get('/inspections')
}

export function getInspectionsByPermit(permitId) {
  return request.get(`/inspections/permit/${permitId}`)
}

export function getInspectionsByInspector(inspectorId) {
  return request.get(`/inspections/inspector/${inspectorId}`)
}

export function getInspectionById(id) {
  return request.get(`/inspections/${id}`)
}

export function createInspection(data) {
  return request.post('/inspections', data)
}
