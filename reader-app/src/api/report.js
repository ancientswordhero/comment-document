import api from './index'

export function reportReview(reviewId, data) {
  return api.post(`/reviews/${reviewId}/report`, data)
}

export function getNotifications(params = {}) {
  return api.get('/notifications', { params })
}

export function getUnreadCount() {
  return api.get('/notifications/unread-count')
}

export function markAsRead(notificationId) {
  return api.put(`/notifications/${notificationId}/read`)
}

export function markAllAsRead() {
  return api.put('/notifications/read-all')
}

export function reportNote(noteId, data) {
  return api.post('/reports', { ...data, noteId, targetType: 'note' })
}
