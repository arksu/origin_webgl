import {Application, Container, Sprite, utils} from "pixi.js"
import Client from "@/net/Client";
import Grid from "@/game/Grid";
import Tile from "@/game/Tile";

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

    /**
     * контейнер в котором храним контейнеры с гридами и тайлами
     * их координаты внутри абсолютные мировые экранные
     * @private
     */
    private mapGrids: Container;

    private grids: Grid[] = [];

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

        this.mapGrids = new Container();
        this.app.stage.addChild(this.mapGrids);

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
        this.mapGrids.destroy({children: true})

        // for (let i = 0; i < this.grids.length; i++) {
        //     this.grids[i].destroy()
        // }
    }

    private setup() {
        for (let key in Client.instance.map) {
            let splitted = key.split("_");
            let x: number = +splitted[0];
            let y: number = +splitted[1];

            let grid: Grid = new Grid(this.app, x, y);

            this.mapGrids.addChild(grid.container);
            this.grids.push(grid);
        }

        let px = Client.instance.playerPos!!.x;
        let py = Client.instance.playerPos!!.y;
        console.log("px=" + px + " py=" + py);

        let sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
        let sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;

        console.log("sx=" + sx + " sy=" + sy);

        let mul = 0.5

        this.mapGrids.x = this.app.renderer.width / 2 - sx * mul;
        this.mapGrids.y = this.app.renderer.height / 2 - sy * mul;


        this.mapGrids.scale.x = mul;
        this.mapGrids.scale.y = mul;

        let cross = Sprite.from("cross_temp.png");
        console.log(cross)
        this.app.stage.addChild(cross)
        cross.pivot.x = 0.5
        cross.pivot.y = 0.5
        cross.x = this.app.renderer.width / 2
        cross.y = this.app.renderer.height / 2 - 22
    }
}