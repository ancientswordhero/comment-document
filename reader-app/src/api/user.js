import api from './index'

export function getUserProfile(userId) {
  return api.get(`/users/${userId}`)
}

export function updateProfile(data) {
  return api.put('/users/profile', data)
}
