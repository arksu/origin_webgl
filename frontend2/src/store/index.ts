import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', {
    state: () => ({
        counter: 0,
        name: 'Eduardo',
        ssid: localStorage.getItem('ssid')
    }),
    getters: {
        isLogged: (state) => {
            return state.ssid != null
        }
    },
    actions: {}
})
