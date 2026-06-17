import request from '@/utils/request'

export function getRectificationReviews(params) {
  return request.get('/rectification-reviews', { params })
}

export function getRectificationReviewById(id) {
  return request.get(`/rectification-reviews/${id}`)
}

export function getRectificationReviewByNo(reviewNo) {
  return request.get(`/rectification-reviews/no/${reviewNo}`)
}

export function createRectificationReview(data) {
  return request.post('/rectification-reviews', data)
}
