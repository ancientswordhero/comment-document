import axios from 'axios'

const api = axios.create({
  baseURL: '/api/auth',
  timeout: 10000
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
    return Promise.reject(new Error(msg))
  }
)

export default api