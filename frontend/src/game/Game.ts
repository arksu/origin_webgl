import {Application, utils} from "pixi.js"
import Client from "@/net/Client";
import Grid from "@/game/Grid";

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
        Client.instance.clear();

        this.canvas.style.display = "none";
        this.appdiv.style.display = "block";
    }

    constructor() {
        this.app = new Application({view: Game.canvas, autoDensity: false});
        this.app.renderer.backgroundColor = 0x333333;
        this.app.renderer.resize(window.innerWidth, window.innerHeight);

        let loader = this.app.loader;

        let img = utils.TextureCache['assets/tiles.json_image'];

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
        for (let key in Client.instance.map) {
            let splitted = key.split("_");
            let x: number = +splitted[0];
            let y: number = +splitted[1];

            let grid: Grid = new Grid(this.app, x, y);
            this.app.stage.addChild(grid.container);
        }
    }
}