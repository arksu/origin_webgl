import {Application, Sprite, Texture, utils} from "pixi.js"
import {getRandomInt} from "@/utils/Util";

let PIXI = require("pixi.js");

/**
 * основная игровая логика (графика и тд)
 */
export default class Game {

    private static readonly canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById("game");
    private static readonly appdiv: HTMLElement = <HTMLElement>document.getElementById("app");

    private static instance?: Game;

    /**
     * PIXI app
     */
    private app: Application;

    public static start() {
        console.warn("pixi start");


        this.canvas.style.display = "block";
        this.appdiv.style.display = "none";

        this.instance = new Game();
    }

    public static stop() {
        console.warn("pixi stop");

        Game.instance?.destroy();
        Game.instance = undefined;

        this.canvas.style.display = "none";
        this.appdiv.style.display = "block";
    }

    constructor() {
        this.app = new Application({view: Game.canvas, autoDensity: false});
        this.app.renderer.backgroundColor = 0x333333;
        this.app.renderer.resize(window.innerWidth, window.innerHeight);

        let loader = this.app.loader;

        let img = utils.TextureCache['assets/tiles.json_image']
        if (img == undefined) {
            loader.add("assets/tiles.json")
            loader.load((_, __) => {
                this.setup();
            })
        } else {
            this.setup();
        }
    }

    private destroy() {
        this.app.destroy();
    }

    private setup() {
        const TILE_WIDTH_HALF = 64 / 2;
        const TILE_HEIGHT_HALF = 32 / 2;

        for (let x = 0; x < 20; x++) {
            for (let y = 0; y < 15; y++) {

                let rnd = getRandomInt(2);
                let tn = rnd == 1 ? 'grass1.png' : 'grass2.png';

                let t = Texture.from(tn);

                let s = Sprite.from(t);
                s.roundPixels = true;

                this.app.stage.addChild(s)

                s.x = 500 + x * TILE_WIDTH_HALF - y * TILE_WIDTH_HALF;
                s.y = 200 + x * TILE_HEIGHT_HALF + y * TILE_HEIGHT_HALF;
            }
        }
    }
}