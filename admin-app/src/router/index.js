import { createRouter, createWebHistory } from 'vue-router'
import BookTable from '../views/BookTable.vue'
import BookForm from '../views/BookForm.vue'
import AdminForm from '../views/AdminForm.vue'

const routes = [
  { path: '/', name: 'book-list', component: BookTable },
  { path: '/book/new', name: 'book-new', component: BookForm },
  { path: '/book/:id/edit', name: 'book-edit', component: BookForm, props: true },
  { path: '/admins', name: 'admin-form', component: AdminForm }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token) {
    window.location.href = 'http://localhost:5174'
    return
  }
  next()
})

export default router
