import api from './index'

export function deleteAccount() {
  return api.delete('/auth/me')
}
