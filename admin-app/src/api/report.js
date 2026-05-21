import { adminApi } from './index'

export function getReports(params) {
  return adminApi.get('/reports', { params })
}

export function resolveReport(id, data) {
  return adminApi.put(`/reports/${id}/resolve`, data)
}
