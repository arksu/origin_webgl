import { defineStore } from 'pinia'
import { readonly, ref } from 'vue'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || undefined)

  const setToken = (newToken : string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
    router.push({ name: RouteNames.CHARACTERS })
  }

  const logout = () => {
    token.value = undefined
    localStorage.removeItem('token')
    router.push({ name: RouteNames.LOGIN })
  }

  return {token : readonly(token), setToken, logout}
})