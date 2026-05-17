import { createRouter, createWebHistory } from 'vue-router'
import BookList from '../views/BookList.vue'
import BookDetail from '../views/BookDetail.vue'

const routes = [
  { path: '/', name: 'home', component: BookList },
  { path: '/book/:id', name: 'book-detail', component: BookDetail }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
