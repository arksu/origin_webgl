import { defineStore } from 'pinia'
import { readonly, ref } from 'vue'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || undefined)
  const websocketToken = ref<string | undefined>(undefined)
  const lastError = ref<string | undefined>(undefined)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
    router.push({ name: RouteNames.CHARACTERS })
  }

  const clearToken = () => {
    token.value = undefined
    localStorage.removeItem('token')
  }

  const logout = () => {
    token.value = undefined
    localStorage.removeItem('token')
    router.push({ name: RouteNames.LOGIN })
  }

  return { lastError, token: readonly(token), setToken, clearToken, websocketToken, logout }
})