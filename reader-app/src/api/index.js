import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    if (response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response.data.data
  },
  error => {
    const msg = error.response?.data?.message || '网络错误'
    if (error.response?.status === 401) {
      alert(msg)
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = 'http://localhost:5176'
      return Promise.reject(error)
    }
    console.error(msg)
    return Promise.reject(error)
  }
)

export default api