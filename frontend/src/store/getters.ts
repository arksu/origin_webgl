import {GetterTree} from 'vuex'
import {State} from './state'

export type Getters = {
    isLogged(state: State): boolean
    ssid(state: State): undefined | string
}

export const getters: GetterTree<State, State> & Getters = {
    isLogged: (state) => {
        return state.ssid !== undefined
    },
    ssid: (state) => {
        return state.ssid
    }
}