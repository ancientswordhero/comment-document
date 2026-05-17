import { createRouter, createWebHistory } from 'vue-router'
import BookTable from '../views/BookTable.vue'
import BookForm from '../views/BookForm.vue'

const routes = [
  { path: '/', name: 'book-list', component: BookTable },
  { path: '/book/new', name: 'book-new', component: BookForm },
  { path: '/book/:id/edit', name: 'book-edit', component: BookForm, props: true }
]

export default createRouter({ history: createWebHistory(), routes })
