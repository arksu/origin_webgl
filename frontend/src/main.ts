import '@fortawesome/fontawesome-free/js/fontawesome'
import '@fortawesome/fontawesome-free/js/solid'

import router from "@/router";
import App from "@/App.vue";

import "@/scss/main.scss";
import {createApp} from "vue";
import Client from "@/net/Client";
import Game from "@/game/Game";

// формируем ссылку для работы с бекендом
let gameServerPort = 8020;
let proto = "https:" === window.location.protocol ? "wss:" : "ws:";
Client.wsUrl = proto + "//" + window.location.hostname + ":" + gameServerPort + "/game";
Client.apiUrl = window.location.protocol + "//" + window.location.hostname + ":" + gameServerPort

// создадим синглтон для клиента где будем хранить наш игровой стейт
Client.instance = new Client();

// навешиваем на канвас обработчики
Game.initCanvasHandlers();

const app = createApp(App);
app.use(router)
app.mount("#app");

// добавим свою кастомную директиву для автофокуса input (v-focus)
// используем в форме логина и регистрации
app.directive('focus', {
    mounted(el) {
        el.focus();
    }
})
