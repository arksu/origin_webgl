import * as PIXI from "pixi.js";
import {Net} from "./Net";

export class Core {
    private gameWidth: number;
    private gameHeight: number;

    public readonly app: PIXI.Application;

    private readonly net: Net;

    constructor() {
        this.gameWidth = window.innerWidth;
        this.gameHeight = window.innerHeight;

        let options: PIXI.ApplicationOptions = {};
        options.view = <HTMLCanvasElement>document.getElementById("game");
        options.backgroundColor = 0x222222;

        // init PIXI
        this.app = new PIXI.Application(this.gameWidth, this.gameHeight, options);
        console.log("game size: " + this.gameWidth + "x" + this.gameHeight);

        // disable loading label
        document.getElementById("loadingLabel").style.display = "none";

        this.net = new Net(this);
    }

    public start() {
        let login: string;
        let password: string;

        login = localStorage.getItem("login");
        password = localStorage.getItem("password");

        console.log(login);
        if (login !== null && password !== null) {
            this.net.login(login, password);
        } else {
            // this.startPixi();
            this.showLogin();
        }
    }

    public showLogin() {
        let authPlug = document.getElementById("auth-plug");
        authPlug.style.display = "none";

        let loginForm = document.getElementById("login-form");
        //console.log(loginForm);
        loginForm.reset();

        let password = document.getElementById("input-password");
        //console.log(password);
        password.value = "";

        loginForm.style.display = "block";

        let gamediv = document.getElementById("game");
        gamediv.style.display = "none";
    }

    public startPixi() {
        console.log("start PIXI");
        let texture = PIXI.Texture.fromImage('assets/logo.png');
        let logo = new PIXI.Sprite(texture);

        this.app.stage.addChild(logo);

        logo.anchor.x = 0.5;
        logo.anchor.y = 0.5;
        logo.position.x = this.gameWidth / 2;
        logo.position.y = this.gameHeight / 3;

        this.net.start();

        this.app.ticker.add((d) => {

        })
    }

    public changeResolution(w: number, h: number) {
        if (this.gameWidth !== w || this.gameHeight !== h) {
            console.log("changeResolution: " + w + "x" + h);
            this.gameWidth = w;
            this.gameHeight = h;
            this.app.renderer.resize(w, h);
            // this.sizeHandler();
        }
    };
}
