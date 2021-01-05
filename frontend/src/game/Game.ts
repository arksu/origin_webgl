import * as PIXI from 'pixi.js';

import Client from "@/net/Client";
import Grid from "@/game/Grid";
import Tile from "@/game/Tile";


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
    private app: PIXI.Application;

    /**
     * контейнер в котором храним контейнеры с гридами и тайлами
     * их координаты внутри абсолютные мировые экранные
     */
    private mapGrids: PIXI.Container;

    /**
     * загруженные гриды
     */
    private grids: Grid[] = [];

    /**
     * невидимый спрайт на весь экран для обработки кликов мыши
     */
    private screenSprite: PIXI.Sprite;

    /**
     * масштаб карты и игровой графики
     */
    private scale: number = 1;

    private crossTemp ?: PIXI.Sprite;

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
        this.app = new PIXI.Application({
            view: Game.canvas,
            autoDensity: true,
            antialias: false,
            backgroundColor: 0x333333
        });
        this.app.renderer.resize(window.innerWidth, window.innerHeight);
        console.warn('render set to ' + this.app.renderer.width + ' ' + this.app.renderer.height)

        this.mapGrids = new PIXI.Container();
        // this.mapGrids.width = this.app.renderer.width
        // this.mapGrids.height = this.app.renderer.height

        this.app.stage.addChild(this.mapGrids);

        this.screenSprite = new PIXI.Sprite();
        this.app.stage.addChild(this.screenSprite);
        this.screenSprite.x = 0;
        this.screenSprite.y = 0;
        this.screenSprite.width = this.app.renderer.width
        this.screenSprite.height = this.app.renderer.height
        this.screenSprite.interactive = true;
        // this.screenSprite.buttonMode = true;
        this.screenSprite.on('mousedown', this.onMouseDown);
        this.screenSprite.on('touchstart', this.onMouseDown);
        this.screenSprite.on('mouseup', this.onMouseUp);
        this.screenSprite.on('touchend', this.onMouseUp);
        this.screenSprite.on('mousewheel', this.onMouseWheel);


        let loader = this.app.loader;

        let img = PIXI.utils.TextureCache['assets/tiles.json_image'];
        // img = undefined

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
        this.app.destroy(false, {
            children: true,
            // texture: true,
            // baseTexture: true
        });

        for (let i = 0; i < this.grids.length; i++) {
            this.grids[i].destroy()
        }
        this.mapGrids.destroy({children: true})
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
        this.crossTemp = PIXI.Sprite.from("cross_temp.png");
        this.app.stage.addChild(this.crossTemp)

        this.updateMapScalePos();
    }

    private onMouseDown(e: PIXI.InteractionEvent) {
        let x = Math.round(e.data.global.x);
        let y = Math.round(e.data.global.y);

        console.log('onMouseDown ' + x + ' ' + y)
    }

    private onMouseUp(e: PIXI.InteractionEvent) {
        let x = Math.round(e.data.global.x);
        let y = Math.round(e.data.global.y);

        console.log('onMouseUp ' + x + ' ' + y)
    }

    private onMouseWheel(delta: number) {
        if (this.scale < 1) {
            this.scale += delta / (1000) * (this.scale * 0.5);
        } else {
            this.scale += delta / (1000);
        }
        if (this.scale < 0.05) this.scale = 0.05
        console.log("scale=" + this.scale)
        this.updateMapScalePos();
    }

    private updateMapScalePos() {
        let px = Client.instance.playerPos!!.x;
        let py = Client.instance.playerPos!!.y;
        console.log("px=" + px + " py=" + py);

        let sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
        let sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;

        console.log("sx=" + sx + " sy=" + sy);

        this.mapGrids.x = this.app.renderer.width / 2 - sx * this.scale;
        this.mapGrids.y = this.app.renderer.height / 2 - sy * this.scale;

        this.mapGrids.scale.x = this.scale;
        this.mapGrids.scale.y = this.scale;

        if (this.crossTemp) {
            this.crossTemp.scale.set(this.scale);
            this.crossTemp.x = this.app.renderer.width / 2 - 17 * this.scale
            this.crossTemp.y = this.app.renderer.height / 2 - 23 * this.scale
        }
    }

    /**
     * навешиваем на канвас обработчик колеса прокрутки
     */
    public static initCanvasZoom() {
        this.canvas.addEventListener('wheel', (e: WheelEvent) => {
            e.preventDefault();
            this.instance?.onMouseWheel(-e.deltaY);
        })
    }
}