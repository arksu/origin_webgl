import { defineStore } from 'pinia'
import { RouteNames } from '@/router/routeNames'
import router from '@/router'

export const useGameStore = defineStore('game', {
  state: () => ({
    selectedCharacterId: undefined as number | undefined
  }),
  actions: {
    logout() {
      localStorage.setItem('wasLogout', '1')
      router.push({ name: RouteNames.CHARACTERS });
    }
  }
})