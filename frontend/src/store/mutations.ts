import {MutationTypes} from './mutation-types'
import {State} from './state'
import {InventoryUpdate} from "@/net/Packets";
import {MutationTree} from "vuex";

export type Mutations<S = State> = {
    [MutationTypes.INVENTORY_UPDATE](state: S, payload: InventoryUpdate): void
    [MutationTypes.INVENTORIES_CLEAR](state: S): void
}

export const mutations: MutationTree<State> & Mutations = {
    [MutationTypes.INVENTORY_UPDATE](state, payload: InventoryUpdate) {
        state.inventories.push(payload)
    },
    [MutationTypes.INVENTORIES_CLEAR](state) {
        state.inventories = []
    }
}