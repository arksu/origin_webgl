import { defineStore } from "pinia";
import router from "../router";
import { RouteNames } from "../router/routeNames";

export const useMainStore = defineStore("main", {
  state: () => ({
    // текст последней ошибки при запросе к апи
    lastError: null as string | null,

    // ид сессии (если залогинены)
    ssid: null as string | null,

    // токен для авторизации ws сессии
    websocketToken: null as string | null,

    // был выполнен автологин по сохраненным учетным данным?
    wasAutoLogin: false,

    gameActive: false,

    autologinCompleted: false,
  }),
  getters: {
    isLogged: (state) => {
      return state.ssid != null;
    },
  },
  actions: {
    // был выполнен успешный вход (авторизация или регистрация)
    onSuccessLogin(ssid: string) {
      this.ssid = ssid;
      router.push({ name: RouteNames.CHARACTERS });
    },
    // выйти (разлогиниться)
    logout() {
      this.ssid = null;
      localStorage.removeItem("hash");
      router.push({ name: RouteNames.LOGIN });
    },
    // произошла ошибка в игровом протоколе
    onGameError(msg: string) {
      this.lastError = msg;
      // в режиме разработки - не удаляем хэш
      if (localStorage.getItem("dev") !== "1") {
        localStorage.removeItem("hash");
      }
      this.wasAutoLogin = true;
      router.push({ name: RouteNames.LOGIN });
    },
  },
});
