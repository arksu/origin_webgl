import { defineStore } from 'pinia'
import { RouteNames } from '@/router/routeNames'
import router from '@/router'
import GameClient from '@/net/GameClient'

export const useGameStore = defineStore('game', {
  state: () => ({
    client: undefined as GameClient | undefined,
    chatLines: [] as ChatItem[],
    chatHistory: [] as string[]
  }),
  actions: {
    logout() {
      localStorage.setItem('wasLogout', '1')
      router.push({ name: RouteNames.CHARACTERS })
    }
  }
})

export type ChatItem = {
  title: string,
  text: string,
  channel: number
}