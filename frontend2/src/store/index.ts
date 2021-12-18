import {defineStore} from 'pinia'
import router from "../router";
import {RouteNames} from "../router/routeNames";

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
    actions: {
        successLogin(ssid: string) {
            this.ssid = ssid
            router.push({name: RouteNames.CHARACTERS})
        }
    }
})
