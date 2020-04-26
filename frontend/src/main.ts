import "./scss/main.scss";
import * as _ from "lodash";
import Net from "./net/Net";
import {doLogin, setLoginForm} from "./login";
import Client from "./net/Client";
import Game from "./game/Game";

window._ = _;


window.onload = function () {
    setNet();
    setLoginForm();

    let login = localStorage.getItem("login");
    let password = localStorage.getItem("password");
    if (login && password) {
        let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
        loginBtn.disabled = true;
        doLogin(login, password);
    } else {
        document.getElementById("login-page").style.display = "block";
    }

    // startPixi();
};

function setNet() {
    let proto = "https:" === window.location.protocol ? "wss" : "ws";
    let net = new Net();
    net.url = proto + "://" + window.location.hostname + ":7070";
    console.log("Net url: " + net.url);

    Net.instance = net;

    net.onDisconnect = () => {
        Client.instance = null;
        Game.stop();
        console.log("net disconnected");
    };
}




