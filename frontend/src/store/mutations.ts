import {MutationTypes} from './mutation-types'
import {State} from './state'
import {HandData, InventoryUpdate} from "@/net/Packets";
import {MutationTree} from "vuex";

export type Mutations<S = State> = {
    [MutationTypes.SET_SSID](state: S, payload: undefined | string): void
    [MutationTypes.SET_LAST_ERROR](state: S, payload: undefined | string): void
    [MutationTypes.INVENTORY_UPDATE](state: S, payload: InventoryUpdate): void
    [MutationTypes.INVENTORY_CLOSE](state: S, payload: number): void
    [MutationTypes.INVENTORIES_CLEAR](state: S): void
    [MutationTypes.SET_HAND](state: S, payload: HandData): void
}

export const mutations: MutationTree<State> & Mutations = {
    [MutationTypes.INVENTORY_UPDATE](state, payload: InventoryUpdate) {
        const idx = state.inventories.findIndex((e: InventoryUpdate) => {
            return (e.id == payload.id)
        })
        if (idx < 0) {
            state.inventories.push(payload)
        } else {
            state.inventories.splice(idx, 1, payload)
        }
    },
    [MutationTypes.INVENTORY_CLOSE](state, payload: number) {
        const idx = state.inventories.findIndex(i => i.id == payload)
        state.inventories.splice(idx, 1)
    },
    [MutationTypes.INVENTORIES_CLEAR](state) {
        state.inventories = []
    },
    [MutationTypes.SET_SSID](state, payload: undefined | string) {
        state.ssid = payload
        if (payload !== undefined) {
            localStorage.setItem("ssid", payload);
        } else {
            localStorage.removeItem("ssid");
        }
    },
    [MutationTypes.SET_LAST_ERROR](state, payload: undefined | string) {
        state.lastError = payload
    },
    [MutationTypes.SET_HAND](state, payload: HandData | undefined) {
        state.hand = payload
    }
}