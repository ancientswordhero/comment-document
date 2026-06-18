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
    // 4xx 是业务逻辑响应（校验失败、重复操作等），用 warn 而非 error
    const status = error.response?.status || 0
    if (status >= 500) console.error(msg)
    else console.warn(msg)
    return Promise.reject(error)
  }
)

export default api