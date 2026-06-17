import api from './index'

export function createNote(bookId, data) {
  return api.post(`/books/${bookId}/notes`, data)
}

export function getMyNotes(params = {}) {
  return api.get('/notes/mine', { params })
}

export function getMyNotesForBook(bookId) {
  return api.get(`/books/${bookId}/notes/mine`)
}

export function updateNote(noteId, data) {
  return api.put(`/notes/${noteId}`, data)
}

export function deleteNote(noteId) {
  return api.delete(`/notes/${noteId}`)
}

export function publishNote(noteId) {
  return api.post(`/notes/${noteId}/publish`)
}

export function unpublishNote(noteId) {
  return api.post(`/notes/${noteId}/unpublish`)
}

export function getPublicNotes(params = {}) {
  return api.get('/notes/public', { params })
}

export function getPublicNotesForBook(bookId, params = {}) {
  return api.get(`/books/${bookId}/notes/public`, { params })
}

export function replyNote(noteId, data) {
  return api.post(`/notes/${noteId}/reply`, data)
}

export function toggleLikeNote(noteId) {
  return api.post(`/notes/${noteId}/like`)
}
