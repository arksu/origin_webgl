import {defineStore} from 'pinia'
import router from "../router";
import {RouteNames} from "../router/routeNames";

export const useMainStore = defineStore('main', {
    state: () => ({
        // текст последней ошибки при запросе к апи
        lastError: null as string | null,

        // ид сессии (если залогинены)
        ssid: localStorage.getItem('ssid'),

        // был выполнен автологин по сохраненным учетным данным?
        wasAutoLogin: false
    }),
    getters: {
        isLogged: (state) => {
            return state.ssid != null
        }
    },
    actions: {
        // был выполнен успешный вход (авторизация или регистрация)
        onSuccessLogin(ssid: string) {
            this.ssid = ssid
            router.push({name: RouteNames.CHARACTERS})
        },
        // выйти (разлогиниться)
        logout() {
            this.ssid = null
            router.push({name: RouteNames.LOGIN})
        }
    }
})
