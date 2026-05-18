import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

const urlParams = new URLSearchParams(window.location.search)
const token = urlParams.get('token')
if (token) {
  localStorage.setItem('token', token)
  window.history.replaceState({}, '', window.location.pathname)
}

createApp(App).use(router).mount('#app')
