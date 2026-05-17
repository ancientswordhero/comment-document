import api from './index'
import axios from 'axios'

const readerApi = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

export function getCategories() {
  return readerApi.get('/categories').then(r => r.data)
}

export function getBooks(params) {
  return api.get('/books', { params })
}

export function getBookById(id) {
  return api.get(`/books/${id}`)
}

export function createBook(data) {
  return api.post('/books', data)
}

export function updateBook(id, data) {
  return api.put(`/books/${id}`, data)
}

export function deleteBook(id) {
  return api.delete(`/books/${id}`)
}

export function toggleStatus(id) {
  return api.put(`/books/${id}/status`)
}

export function uploadCover(file) {
  const form = new FormData()
  form.append('file', file)
  return api.post('/upload/cover', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
