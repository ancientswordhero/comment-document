import { ref } from 'vue'
import { getUnreadCount } from '../api/report'

// 全局共享的未读通知数
const unreadCount = ref(0)

export function useUnreadBadge() {
  async function fetchUnreadCount() {
    try {
      unreadCount.value = await getUnreadCount()
    } catch { /* ignore */ }
  }

  function markOneRead() {
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  function markAllRead() {
    unreadCount.value = 0
  }

  function setCount(n) {
    unreadCount.value = n
  }

  return {
    unreadCount,
    fetchUnreadCount,
    markOneRead,
    markAllRead,
    setCount
  }
}
