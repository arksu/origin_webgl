import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', {
    state: () => ({
        lastError: null as string | null,
        ssid: localStorage.getItem('ssid')
    }),
    getters: {
        isLogged: (state) => {
            return state.ssid != null
        }
    },
    actions: {}
})
