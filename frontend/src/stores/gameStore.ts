import { defineStore } from 'pinia'
import { RouteNames } from '@/router/routeNames'
import router from '@/router'
import GameClient from '@/net/GameClient'
import type { ContextMenuData, HandData, InventoryUpdate } from '@/net/packets'

export const useGameStore = defineStore('game', {
  state: () => ({
    client: undefined as GameClient | undefined,

    chatLines: [] as ChatItem[],
    chatHistory: [] as string[],

    contextMenu: undefined as ContextMenuData | undefined,
    contextMenuPosX : 0 as number,
    contextMenuPosY : 0 as number,

    inventories: [] as InventoryUpdate[],
    hand: undefined as HandData | undefined,

    actionProgress: {
      total: 0,
      current: 0
    } as ActionProgress,

  }),
  getters: {
    /**
     * ищем инвентарь по ид
     * @return InventoryUpdate | undefined
     */
    getInventoryById(state) {
      return (id: number) => {
        const idx = this.inventories.findIndex(inventory => inventory.id == id)
        return (idx >= 0) ? state.inventories[idx] : undefined
      }
    },
    /**
     * номер кадра (часов) для выполняемого действия
     */
    actionFrame(): number {
      return Math.round((this.actionProgress.current / this.actionProgress.total) * 21)
    },

  },
  actions: {
    setInventory(pkt: InventoryUpdate) {
      const idx = this.inventories.findIndex(inventory => inventory.id == pkt.id)
      if (idx < 0) {
        this.inventories.push(pkt)
      } else {
        this.inventories[idx] = pkt
      }
    },
    closeInventory(id: number) {
      const idx = this.inventories.findIndex(inventory => inventory.id == id)
      if (idx >= 0) this.inventories.splice(idx, 1)
    },
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

export type ActionProgress = {
  total: number,
  current: number
}