import "./scss/main.scss";
import * as _ from "lodash";
import * as PIXI from "pixi.js";
import Net from "./net/Net";
import ApplicationOptions = PIXI.ApplicationOptions;

window._ = _;

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

        // loginForm.style.display = "none";

        // net.login(
        //     loginField.value,
        //     passwordField.value
        // );

        return false;
    };

    document.getElementById("register-btn").onclick = function (e) {
        e.preventDefault();

        loginForm.style.display = "none";
        registerForm.style.display = "block";
    };
    document.getElementById("sign-btn").onclick = function (e) {
        e.preventDefault();

        loginForm.style.display = "block";
        registerForm.style.display = "none";
    };
}

window.onload = function () {
    setLoginForm();

    let proto = "https:" === window.location.protocol ? "wss" : "ws";
    let net = new Net();
    net.url = proto + "://" + window.location.hostname + ":7070";
    console.log("url: " + net.url);

    net.onDisconnect = () => {
        console.log("net disconnected");
    };
    net.remoteCall("login", {
        login: "root",
        password: "root"
    });


    // startPixi();


    console.log("yay!");


};

