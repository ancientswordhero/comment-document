import api from './index'

export function getReviews(bookId, params = {}) {
  return api.get(`/books/${bookId}/reviews`, { params })
}

export function createReview(bookId, data) {
  return api.post(`/books/${bookId}/reviews`, data)
}

export function createReply(reviewId, data) {
  return api.post(`/reviews/${reviewId}/reply`, data)
}

export function updateReview(reviewId, data) {
  return api.put(`/reviews/${reviewId}`, data)
}

export function deleteReview(reviewId) {
  return api.delete(`/reviews/${reviewId}`)
}

export function toggleLike(reviewId) {
  return api.post(`/reviews/${reviewId}/like`)
}
