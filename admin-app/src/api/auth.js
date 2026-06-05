import axios from 'axios'

const authApi = axios.create({
  baseURL: '/api/auth',
  timeout: 10000
})

authApi.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

authApi.interceptors.response.use(
  response => {
    if (response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response.data.data
  }
)

export function createAdmin(username, password) {
  return authApi.post('/admin', { username, password })
}
