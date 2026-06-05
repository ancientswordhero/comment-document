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

export function createBook(formData, onProgress) {
  return adminApi.post('/books', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress
  })
}

export function updateBook(id, formData, onProgress) {
  return adminApi.put(`/books/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress
  })
}

export function deleteBook(id) {
  return adminApi.delete(`/books/${id}`)
}

export function toggleStatus(id) {
  return adminApi.put(`/books/${id}/status`)
}
