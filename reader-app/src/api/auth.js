import api from './index'

export function getMe(token) {
  return api.get('/auth/me', {
    headers: { Authorization: `Bearer ${token}` }
  })
}
