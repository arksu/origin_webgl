import {defineStore} from "pinia";
import {HandData, InventoryUpdate, InvItem, TimeUpdate} from "../net/packets";
import GameClient from "../net/GameClient";

export type ChatItem = {
    title: string,
    text: string,
    channel: number
}

export type ActionProgress = {
    total: number,
    current: number
}

const SUN_ANGLE_MULT = 0.6
const SUN_ANGLE_OFFSET = 0.2

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

        chatLines: [] as ChatItem[],
        chatHistory: [] as string[],

        actionProgress: {
            total: 0,
            current: 0
        } as ActionProgress,

        inventories: [] as InventoryUpdate[],

        hand: undefined as HandData | undefined,

        time: undefined as TimeUpdate | undefined,
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
        },
        sunX(): number {
            if (this.time !== undefined) {
                return -Math.cos((this.time.sv / 255.0) * (Math.PI + SUN_ANGLE_MULT) - SUN_ANGLE_OFFSET)
            } else return 0
        },
        sunY(): number {
            if (this.time !== undefined) {
                return -Math.sin((this.time.sv / 255.0) * (Math.PI + SUN_ANGLE_MULT) - SUN_ANGLE_OFFSET)
            } else return 0
        },
        /**
         * ищем инвентарь по ид
         * @param state
         * @return InventoryUpdate | undefined
         */
        getInventoryById(state) {
            return (id: number) => {
                const idx = state.inventories.findIndex((e: InventoryUpdate) => {
                    return e.id == id
                })
                return (idx >= 0) ? state.inventories[idx] : undefined
            }
        },
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
        closeInventory(id: number) {
            const idx = this.inventories.findIndex(i => i.id == id)
            this.inventories.splice(idx, 1)
        },
        toggleInventory() {
            console.log('openInventory')
            const selectedCharacterId = this.selectedCharacterId;
            if (selectedCharacterId != undefined) {
                if (this.getInventoryById(selectedCharacterId) == undefined) {
                    GameClient.remoteCall("openmyinv")
                } else {
                    GameClient.remoteCall("invclose", {
                        iid: selectedCharacterId
                    })
                }
            }
        },
    }
})
