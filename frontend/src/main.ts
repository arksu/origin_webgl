import {createApp} from 'vue'
import router from "./router";
import App from "./App.vue";

import "./scss/main.scss";
import Net from "./net/Net";
import Client from "./net/Client";
import Game from "./game/Game";


createApp(App)
    .use(router)
    .mount("#app");

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




