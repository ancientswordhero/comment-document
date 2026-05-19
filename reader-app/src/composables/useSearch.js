import { ref } from 'vue'

const searchKeyword = ref('')

export function useSearchState() {
  return {
    searchKeyword
  }
}

export function setSearchKeyword(keyword) {
  searchKeyword.value = keyword
}