import "./scss/main.scss";
import * as _ from "lodash";
import * as PIXI from "pixi.js";
import Net from "./net/Net";
import {setLoginForm} from "./login";

window._ = _;


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

    let opt: PIXI.ApplicationOptions = {
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




