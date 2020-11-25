import router from "@/router";
import App1 from "@/App.vue";

import "@/scss/main.scss";
import Net from "@/net/Net";
import Game from "@/game/Game";
import {createApp} from "vue";
import Client from "@/net/Client";

Client.instance = new Client();

// пробуем поднять сеть

// смотрим есть ли у нас сохраненный логин и пароль (хэш)

// авторизуемся на сервере

const app = createApp(App1);
app.use(router)
app.mount("#app");

// добавим свою кастомную директиву для автофокуса input (v-focus)
// используем в форме логина и регистрации
app.directive('focus', {
    mounted(el) {
        el.focus();
    }
})

window.onload = function () {
    // setNet();
    // setLoginForm();

    // let login = localStorage.getItem("login");
    // let password = localStorage.getItem("password");
    // if (login && password) {
    //     let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
    //     loginBtn.disabled = true;
    //     doLogin(login, password);
    // } else {
    //     document.getElementById("login-page").style.display = "block";
    // }

    // startPixi();
};

function setNet() {
    let proto = "https:" === window.location.protocol ? "wss" : "ws";
    let net = new Net(proto + "://" + window.location.hostname + ":7070");
    console.log("Net url: " + net.url);

    Net.instance = net;

    net.onDisconnect = () => {
        Game.stop();
        console.log("net disconnected");
    };
}




