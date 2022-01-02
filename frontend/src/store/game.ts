import {defineStore} from "pinia";
import {HandData, InventoryUpdate, InvItem} from "../net/packets";

export type ChatItem = {
    title: string,
    text: string,
    channel: number
}

export type ActionProgress = {
    total: number,
    current: number
}

export const useGameStore = defineStore('game', {
    state: () => ({
        selectedCharacterId: undefined as number | undefined,

        CUR_SHP: 0 as number,
        CUR_HHP: 0 as number,
        MAX_HP: 0 as number,

        CUR_STAMINA: 0 as number,
        MAX_STAMINA: 0 as number,

        CUR_ENERGY: 0 as number,
        MAX_ENERGY: 0 as number,

        chatHistory: [] as ChatItem[],

        actionProgress: {
            total: 0,
            current: 0
        } as ActionProgress,

        inventories: [] as InventoryUpdate[],

        hand : undefined as HandData | undefined,
    }),
    getters: {
        staminaPercent(): number {
            return this.CUR_STAMINA / this.MAX_STAMINA * 100
        },
        softHpPercent(): number {
            return this.CUR_SHP / this.MAX_HP * 100
        },
        hardHpPercent(): number {
            return this.CUR_HHP / this.MAX_HP * 100
        },
        /**
         * номер кадра (часов) для выполняемого действия
         */
        actionFrame(): number {
            return Math.round((this.actionProgress.current / this.actionProgress.total) * 21)
        }
    },
    actions: {
        setInventory(payload: InventoryUpdate) {
            const idx = this.inventories.findIndex((e: InventoryUpdate) => {
                return e.id == payload.id
            })
            if (idx < 0) {
                this.inventories.push(payload)
            } else {
                this.inventories[idx] = payload
            }
        },
        closeInventory(id : number) {
            const idx = this.inventories.findIndex(i => i.id == id)
            this.inventories.splice(idx, 1)
        }
    }
})
