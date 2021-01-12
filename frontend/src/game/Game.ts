import * as PIXI from 'pixi.js';

import Client from "@/net/Client";
import Grid from "@/game/Grid";
import Tile from "@/game/Tile";
import Net from "@/net/Net";
import Point from '@/utils/Point';
import {GameObject} from "@/game/GameObject";

/**
 * основная игровая логика (графика и тд)
 */
export default class Game {

    private static readonly canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById("game");
    private static readonly appDiv: HTMLElement = <HTMLElement>document.getElementById("app");

    public static instance?: Game = undefined;

    /**
     * PIXI app
     */
    private readonly app: PIXI.Application;

    /**
     * контейнер в котором храним контейнеры с гридами и тайлами
     * их координаты внутри абсолютные мировые экранные
     */
    private readonly mapGrids: PIXI.Container;

    /**
     * загруженные гриды
     */
    private grids: Grid[] = [];

    /**
     * невидимый спрайт на весь экран для обработки кликов мыши
     */
    private readonly screenSprite: PIXI.Sprite;

    /**
     * масштаб карты и игровой графики
     */
    private scale: number = 1;

    private crossTemp ?: PIXI.Sprite;

    /**
     * "перетаскивание" карты мышью
     */
    private dragStart ?: Point;
    /**
     * какой был отступ карты в начале перетаскивания
     */
    private dragOffset ?: Point;
    private dragMoved: boolean = false;

    /**
     * отступ в координатах экрана от центра экрана до игрока
     */
    private offset: Point = new Point(0, 0);

    /**
     * список объектов которые в данный момент движутся
     * апдейтим их позицию каждый тик через move controller
     */
    public movingObjects: { [key: number]: GameObject } = {}

    public static start() {
        console.warn("pixi start");


        this.canvas.style.display = "block";
        this.appDiv.style.display = "none";

        this.instance = new Game();
    }

    public static stop() {
        console.warn("pixi stop");

        Game.instance?.destroy();
        Game.instance = undefined;
        Client.instance.clear();

        this.canvas.style.display = "none";
        this.appDiv.style.display = "block";
    }

    constructor() {
        this.app = new PIXI.Application({
            view: Game.canvas,
            autoDensity: true,
            antialias: false,
            backgroundColor: 0x333333
        });
        this.app.renderer.resize(window.innerWidth, window.innerHeight);
        console.warn('render size ' + this.app.renderer.width + ' ' + this.app.renderer.height)

        PIXI.Ticker.shared.add(this.update.bind(this))

        this.mapGrids = new PIXI.Container();

        this.app.stage.addChild(this.mapGrids);

        this.screenSprite = new PIXI.Sprite();
        this.app.stage.addChild(this.screenSprite);
        this.screenSprite.x = 0;
        this.screenSprite.y = 0;
        this.screenSprite.width = this.app.renderer.width
        this.screenSprite.height = this.app.renderer.height
        this.screenSprite.interactive = true;

        this.screenSprite.on('mousedown', this.onMouseDown.bind(this));
        this.screenSprite.on('touchstart', this.onMouseDown.bind(this));
        this.screenSprite.on('mouseup', this.onMouseUp.bind(this));
        this.screenSprite.on('touchend', this.onMouseUp.bind(this));
        this.screenSprite.on('mousemove', this.onMouseMove.bind(this));
        this.screenSprite.on('touchmove', this.onMouseMove.bind(this));
        this.screenSprite.on('mousewheel', this.onMouseWheel.bind(this));


        const loader = this.app.loader;
        const img = PIXI.utils.TextureCache['assets/tiles.json_image'];

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
            let s = key.split("_");
            let x: number = +s[0];
            let y: number = +s[1];

            let grid: Grid = new Grid(this.app, x, y);

            this.mapGrids.addChild(grid.container);
            this.grids.push(grid);
        }
        this.crossTemp = PIXI.Sprite.from("man.png");
        this.app.stage.addChild(this.crossTemp)

        this.updateMapScalePos();
    }

    private onMouseDown(e: PIXI.InteractionEvent) {
        this.dragStart = new Point(e.data.global).round();
        this.dragOffset = new Point(this.offset);
        this.dragMoved = false;
        console.log('onMouseDown ' + this.dragStart.toString());
    }

    private onMouseUp(e: PIXI.InteractionEvent) {
        let p = new Point(e.data.global).round();

        console.log('onMouseUp ' + p.toString());

        if (this.dragStart !== undefined && this.dragOffset !== undefined) {
            let d = new Point(p).dec(this.dragStart);

            // мышь передвинулась достаточно далеко?
            if (Math.abs(d.x) > 10 || Math.abs(d.y) > 10 || this.dragMoved) {
                this.offset.set(this.dragOffset).inc(d);

                this.updateMapScalePos();
            } else {
                // иначе это был просто клик
                let cp = this.coordScreen2Game(p);
                console.log("mapclick " + cp.toString());
                Net.remoteCall("mapclick", {
                    x: cp.x,
                    y: cp.y
                })
            }
            this.dragStart = undefined;
            this.dragOffset = undefined;
        }
    }

    private onMouseMove(e: PIXI.InteractionEvent) {
        if (this.dragStart !== undefined && this.dragOffset !== undefined) {
            let p = new Point(e.data.global).round();

            p.dec(this.dragStart);

            if (Math.abs(p.x) > 10 || Math.abs(p.y) > 10 || this.dragMoved) {
                this.dragMoved = true;
                this.offset.set(this.dragOffset).inc(p);

                this.updateMapScalePos();
            }
        }
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

    private onMouseRightClick(e: MouseEvent) {

    }

    public updateMapScalePos() {

        let px = Client.instance.playerObject.x;
        let py = Client.instance.playerObject.y;

        let sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
        let sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;

        let offx = this.app.renderer.width / 2 + this.offset.x;
        let offy = this.app.renderer.height / 2 + this.offset.y;

        this.mapGrids.x = offx - sx * this.scale;
        this.mapGrids.y = offy - sy * this.scale;

        this.mapGrids.scale.x = this.scale;
        this.mapGrids.scale.y = this.scale;

        if (this.crossTemp) {
            this.crossTemp.scale.set(this.scale);
            // TODO
            this.crossTemp.x = offx - 17 * this.scale;
            this.crossTemp.y = offy - 57 * this.scale;
        }
    }

    private update() {
        // delta time in seconds
        let dt = PIXI.Ticker.shared.deltaMS / 1000

        for (let key in this.movingObjects) {
            let moveController = this.movingObjects[key].moveController
            if (moveController !== undefined) {
                moveController.update(dt)
            }
        }
    }

    /**
     * перевести экранные координаты в игровые
     */
    private coordScreen2Game(p: Point): Point {
        console.log("coordScreen2Game " + p.toString())

        p.dec(this.offset);

        let px = Client.instance.playerObject.x;
        let py = Client.instance.playerObject.y;

        console.log("player pos " + px + " " + py)

        let screenWidthHalf = this.app.renderer.width / 2;
        let screenHeightHalf = this.app.renderer.height / 2;
        p.decValue(screenWidthHalf, screenHeightHalf).mulValue(1 / this.scale);

        return new Point(
            p.y / Tile.TEXTURE_HEIGTH + p.x / Tile.TEXTURE_WIDTH,
            p.y / Tile.TEXTURE_HEIGTH - p.x / Tile.TEXTURE_WIDTH
        ).mulValue(Tile.TILE_SIZE).incValue(px, py).round();
    }

    private static onResize() {
        this.instance?.app.renderer.resize(window.innerWidth, window.innerHeight);
        this.instance?.updateMapScalePos()
    }

    /**
     * навешиваем на канвас обработчики
     */
    public static initCanvasHandlers() {
        // ловим прокрутку страницы и делаем скейл на основе этого
        this.canvas.addEventListener('wheel', (e: WheelEvent) => {
            e.preventDefault();
            this.instance?.onMouseWheel(-e.deltaY);
        });

        // ловим правый клик за счет вызоыва context menu
        this.canvas.addEventListener('contextmenu', (e: Event) => {
            e.preventDefault();
            if (e instanceof MouseEvent) {
                console.log("right click " + e.x + " " + e.y);
                console.log(e.button + " alt=" + e.altKey + " shift=" + e.shiftKey + " meta=" + e.metaKey);
                this.instance?.onMouseRightClick(e);
            }
        });

        // обработчик resize
        let resizeTimeout: any = undefined;
        window.addEventListener('resize', () => {
            if (resizeTimeout == undefined) {
                resizeTimeout = setTimeout(() => {
                    resizeTimeout = undefined;
                    console.log("resize");
                    Game.onResize();
                }, 333);
            }
        });

        window.addEventListener("orientationchange", () => {
            Game.onResize();
        });
    }
}
