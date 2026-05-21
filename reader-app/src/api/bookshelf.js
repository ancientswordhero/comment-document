import api from './index'

export function getBookshelf(page = 1, size = 20) {
  return api.get('/bookshelf', { params: { page, size } })
}

export function addToBookshelf(bookId) {
  return api.post(`/bookshelf/${bookId}`)
}

export function removeFromBookshelf(bookId) {
  return api.delete(`/bookshelf/${bookId}`)
}

export function checkBookshelf(bookId) {
  return api.get(`/bookshelf/${bookId}`)
}
