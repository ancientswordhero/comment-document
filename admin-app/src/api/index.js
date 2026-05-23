import axios from 'axios'

const adminApi = axios.create({
  baseURL: '/api/admin',
  timeout: 10000
})

const publicApi = axios.create({
  baseURL: '/api',
  timeout: 10000
})

function setupInterceptors(api, isAdmin = false) {
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
      if (isAdmin && (error.response?.status === 401 || error.response?.status === 403)) {
        localStorage.removeItem('token')
        window.location.href = 'http://localhost:5174'
      }
      console.error(msg)
      return Promise.reject(error)
    }
  )
}

setupInterceptors(adminApi, true)
setupInterceptors(publicApi, false)

export { adminApi, publicApi }