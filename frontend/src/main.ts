import router from "@/router";
import App from "@/App.vue";

import "@/scss/main.scss";
import Net from "@/net/Net";
import {createApp} from "vue";
import Client from "@/net/Client";

// формируем ссылку для работы с бекендом
let proto = "https:" === window.location.protocol ? "wss:" : "ws:";
Net.url = proto + "//" + window.location.hostname + ":8010";
Net.apiUrl = window.location.protocol + "//" + window.location.hostname + ":8010"

// создадим синглон для клиента где будем хранить наш игровой стейт
Client.instance = new Client();

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
