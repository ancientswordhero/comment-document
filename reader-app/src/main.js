import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

const urlParams = new URLSearchParams(window.location.search)
const token = urlParams.get('token')
if (token) {
  localStorage.setItem('token', token)
  const username = urlParams.get('username')
  if (username) localStorage.setItem('username', username)
  const redirect = urlParams.get('redirect')
  if (redirect) sessionStorage.setItem('redirect', redirect)
  window.history.replaceState({}, '', window.location.pathname)
}

createApp(App).use(router).mount('#app')
