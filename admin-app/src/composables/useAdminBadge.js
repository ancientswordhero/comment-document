import { ref } from 'vue'
import { getPendingCount } from '../api/report'

const pendingCount = ref(0)

export function useAdminBadge() {
  async function fetchPendingCount() {
    try {
      const data = await getPendingCount()
      pendingCount.value = data.count || 0
    } catch { /* ignore */ }
  }

  async function decrementPending() {
    await fetchPendingCount()
  }

  return { pendingCount, fetchPendingCount, decrementPending }
}
