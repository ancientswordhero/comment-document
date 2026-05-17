import api from './index'

export function getBooks(params) {
  return api.get('/books', { params })
}

export function getBookById(id) {
  return api.get(`/books/${id}`)
}

export function getCategories() {
  return api.get('/categories')
}
