import "./scss/main.scss";
import * as _ from "lodash";
import * as PIXI from "pixi.js";
import Net from "./net/Net";
import ApplicationOptions = PIXI.ApplicationOptions;

window._ = _;

let errorMessageTimer;

window.onload = function () {
    setNet();
    setLoginForm();

    // startPixi();
};

function setNet() {
    let proto = "https:" === window.location.protocol ? "wss" : "ws";
    let net = new Net();
    net.url = proto + "://" + window.location.hostname + ":7070";
    console.log("Net url: " + net.url);

    Net.instance = net;

    net.onDisconnect = () => {
        console.log("net disconnected");
    };
}

function startPixi() {
    let canvas = <HTMLCanvasElement>document.getElementById("game");

    let gameWidth = window.innerWidth;
    let gameHeight = window.innerHeight;
    console.log("game: " + gameWidth + " x " + gameWidth);

    let opt: ApplicationOptions = {
        width: gameWidth,
        height: gameHeight,
        backgroundColor: 0x000000,
        view: canvas
    };

    const app = new PIXI.Application(opt);
    app.renderer.autoResize = true;

    let rectangle = new PIXI.Graphics();
    rectangle.beginFill(0x66CCFF);
    rectangle.lineStyle(4, 0xFF3300, 1);
    rectangle.drawRect(10, 20, 200, 100);
    rectangle.endFill();

    app.stage.addChild(rectangle);

    window.onresize = function () {
        app.renderer.resize(window.innerWidth, window.innerHeight);
    };
}

function setLoginForm() {
    let loginForm = document.getElementById("login-form");
    let registerForm = document.getElementById("register-form");

    loginForm.onsubmit = function (e) {
        e.preventDefault();
        console.log("login!");

        let login = (<HTMLInputElement>document.getElementById("input-login")).value;
        let password = (<HTMLInputElement>document.getElementById("input-password")).value;
        let btn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));

        btn.disabled = true;
        doLogin(login, password);
    };

    registerForm.onsubmit = function (e) {
        e.preventDefault();

        console.log("register");

        let login = (<HTMLInputElement>document.getElementById("reg-login")).value;
        let password = (<HTMLInputElement>document.getElementById("reg-password")).value;
        let email = (<HTMLInputElement>document.getElementById("reg-email")).value;
        let btn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("reg-btn"));

        clearLoginError();

        if (!login || !password) {
            showLoginError("Username or password is empty");
        } else {
            btn.disabled = true;
            Net.instance.remoteCall("register", {
                login,
                password,
                email: email
            })
                .then(d => {
                    btn.disabled = false;

                    if (d === "ok") {
                        doLogin(login, password);
                    }
                })
                .catch(e => {
                    console.error(e);
                    if (e === "username busy") {
                        showLoginError("This username is taken");
                    }
                    btn.disabled = false;
                });
        }
    };

    document.getElementById("register-btn").onclick = function (e) {
        e.preventDefault();
        clearLoginError();

        loginForm.style.display = "none";
        registerForm.style.display = "block";
    };
    document.getElementById("sign-btn").onclick = function (e) {
        e.preventDefault();
        clearLoginError();

        loginForm.style.display = "block";
        registerForm.style.display = "none";
    };
}

function showLoginError(msg: string): any {
    const error = document.getElementById("login-error");

    error.innerHTML = msg;
    error.style.display = "block";
    errorMessageTimer = setTimeout(() => {
        error.style.display = "none";
    }, 3000);
}

function clearLoginError() {
    clearTimeout(errorMessageTimer);
    const error = document.getElementById("login-error");
    error.style.display = "none";
}

function doLogin(login: string, password: string) {
    Net.instance.remoteCall("login", {
        login,
        password
    })
        .then((d) => {
            let loginPage = document.getElementById("login-page");
            loginPage.style.display = "none";

        })
        .catch((e) => {
            console.error(e);

            let btn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
            btn.disabled = false;

            showLoginError(e);
        });
}

