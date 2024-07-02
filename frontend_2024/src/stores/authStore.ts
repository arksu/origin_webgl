import { defineStore } from 'pinia'
import { readonly, ref } from 'vue'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || undefined)
  const websocketToken = ref<string | undefined>(localStorage.getItem('websocketToken') || undefined)
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

  const setWebsocketToken = (newToken: string) => {
    console.log('set ws token', newToken)
    websocketToken.value = newToken
    localStorage.setItem('websocketToken', newToken)
  }

  const clearWebsocketToken = () => {
    websocketToken.value = undefined
    localStorage.removeItem('websocketToken')
  }

  const setError = (errorMessage: string | undefined, needClearToken: boolean = true) => {
    lastError.value = errorMessage
    clearToken()
  }

  return {
    lastError, setError,
    logout,
    token: readonly(token), setToken, clearToken,
    websocketToken: readonly(websocketToken), setWebsocketToken, clearWebsocketToken
  }
})