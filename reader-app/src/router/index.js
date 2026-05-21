import { createRouter, createWebHistory } from 'vue-router'
import BookList from '../views/BookList.vue'
import BookDetail from '../views/BookDetail.vue'
import Bookshelf from '../views/Bookshelf.vue'
import Inbox from '../views/Inbox.vue'

const routes = [
  { path: '/', name: 'home', component: BookList },
  { path: '/book/:id', name: 'book-detail', component: BookDetail, meta: { requiresAuth: true } },
  { path: '/bookshelf', name: 'bookshelf', component: Bookshelf, meta: { requiresAuth: true } },
  { path: '/inbox', name: 'inbox', component: Inbox, meta: { requiresAuth: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const redirect = sessionStorage.getItem('redirect')
  if (redirect) {
    sessionStorage.removeItem('redirect')
    window.location.href = redirect
    return
  }

  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    window.location.href = `http://localhost:5176?redirect=${encodeURIComponent(to.fullPath)}`
    return
  }

  next()
})

export default router
