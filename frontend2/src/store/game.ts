import {defineStore} from "pinia";

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
        } as ActionProgress
    }),
    getters: {
        stamina(): number {
            return this.CUR_STAMINA / this.MAX_STAMINA * 100
        },
        softHp(): number {
            return this.CUR_SHP / this.MAX_HP * 100
        },
        hardHp(): number {
            return this.CUR_HHP / this.MAX_HP * 100
        },
        actionFrame(): number {
            return Math.round((this.actionProgress.current / this.actionProgress.total) * 21)
        }
    },
    actions: {}
})
