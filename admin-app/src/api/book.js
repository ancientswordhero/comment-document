import { adminApi, publicApi } from './index'

export function getCategories() {
  return publicApi.get('/categories')
}

export function getBooks(params) {
  return adminApi.get('/books', { params })
}

export function getBookById(id) {
  return adminApi.get(`/books/${id}`)
}

export function createBook(formData) {
  return adminApi.post('/books', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateBook(id, formData) {
  return adminApi.put(`/books/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteBook(id) {
  return adminApi.delete(`/books/${id}`)
}

export function toggleStatus(id) {
  return adminApi.put(`/books/${id}/status`)
}

export function uploadCover(file) {
  const form = new FormData()
  form.append('file', file)
  return adminApi.post('/upload/cover', form)
}