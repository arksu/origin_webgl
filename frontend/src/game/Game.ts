import {Application, Loader, Sprite, utils} from "pixi.js"

let PIXI = require("pixi.js");

/**
 * основная игровая логика (графика и тд)
 */
export default class Game {

    public static start() {
        let canvas = <HTMLCanvasElement>document.getElementById("game");
        let appdiv = <HTMLElement>document.getElementById("app");
        canvas.style.display = "block";
        appdiv.style.display = "none";

        this.startPixi(canvas);
    }

    public static stop() {
        let canvas = <HTMLCanvasElement>document.getElementById("game");
        let appdiv = <HTMLElement>document.getElementById("app");
        canvas.style.display = "none";
        appdiv.style.display = "block";
    }

    private static startPixi(canvas: HTMLCanvasElement) {
        let app = new Application({view: canvas, width: 200, height: 400, autoDensity: false})
        app.renderer.backgroundColor = 0x061639;
        app.renderer.resize(window.innerWidth, window.innerHeight);

        const sprites = {};


        let loader = new Loader()
        loader.add("assets/tiles.json")
        loader.load((_, res) => {
            let s = new Sprite(utils.TextureCache['grass1.png'])
            console.log(s)
            app.stage.addChild(s)

            s.x = 100
            s.y = 200
        })

        // loader.load((loader, resources) => {
        //    let grass1 = new TilingSprite(resources['grass1'])
        // });
    }

    private static setup() {

    }
}