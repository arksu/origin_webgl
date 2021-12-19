import * as PIXI from 'pixi.js';
import GameClient from "../net/GameClient";
import Grid from "./Grid";
import Tile from "./Tile";
import GameObject from "./GameObject";
import Coord from "./Coord";
import ObjectView from "./ObjectView";
import Point from "./Point";
import ActionProgress from "./ActionProgress";

export default class Render {

    public static instance?: Render = undefined;

    /**
     * PIXI app
     */
    private readonly app: PIXI.Application;

    private destroyed: boolean = false

    /**
     * контейнер в котором храним контейнеры с гридами и тайлами
     * их координаты внутри абсолютные мировые экранные
     */
    private readonly mapGrids: PIXI.Container;

    /**
     * контейнер для отображения объектов
     */
    public readonly objectsContainer: PIXI.Container;

    /**
     * невидимый спрайт на весь экран для обработки кликов мыши
     */
    private readonly screenSprite: PIXI.Sprite;

    /**
     * масштаб карты и игровой графики
     */
    private scale: number = 1;

    /**
     * отступ в координатах экрана от центра экрана до игрока
     */
    private offset: Point = new Point(0, 0);

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
     * прикосновения к экрану
     * для обработки мультитача для скейла на мобилках
     */
    private touchCurrent: { [key: number]: Point } = {}

    /**
     * текущее расстояние между пальцами при скейле на мобилках
     */
    private touchLength: number = -1

    /**
     * таймер для непрерывного движения пока нажата ЛКМ
     */
    private continuousMovingTimer: number = -1

    /**
     * последние экранные координаты при нажатой ЛКМ
     */
    private lastMoveCoord ?: Point

    /**
     * загруженные гриды
     */
    private grids: { [key: string]: Grid } = {};

    /**
     * список объектов которые в данный момент движутся
     * апдейтим их позицию каждый тик через move controller
     */
    public movingObjects: { [key: number]: GameObject } = {}

    /**
     * прогресс текущего действия (отображаем по середине экрана)
     */
    private actionProgress ?: ActionProgress

    public static start() {
        console.warn("pixi start");
        this.instance = new Render();
    }

    public static stop() {
        console.warn("pixi stop");

        Render.instance?.destroy();
        Render.instance = undefined;
    }


    private constructor() {
        // сглаживание пикселей при масштабировании
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

        this.app = new PIXI.Application({
            width: window.innerWidth,
            height: window.innerHeight,
            view: <HTMLCanvasElement>window.document.getElementById("game"),
            autoDensity: true,
            preserveDrawingBuffer: true,
            powerPreference: 'high-performance',
            antialias: false,
            backgroundColor: 0x333333
        });

        console.warn('render size ' + this.app.renderer.width + ' ' + this.app.renderer.height)

        this.app.ticker.maxFPS = 60
        this.app.ticker.minFPS = 60

        PIXI.Ticker.shared.add(this.update.bind(this))

        this.mapGrids = new PIXI.Container();
        this.app.stage.addChild(this.mapGrids);

        this.screenSprite = new PIXI.Sprite();
        this.app.stage.addChild(this.screenSprite);

        this.objectsContainer = new PIXI.Container()
        this.objectsContainer.sortableChildren = true
        this.app.stage.addChild(this.objectsContainer)

        this.screenSprite.x = 0;
        this.screenSprite.y = 0;
        this.screenSprite.width = this.app.renderer.width
        this.screenSprite.height = this.app.renderer.height
        this.screenSprite.interactive = true;

/*
        this.screenSprite.on('mousedown', this.onMouseDown.bind(this));
        this.screenSprite.on('touchstart', this.onMouseDown.bind(this));

        this.screenSprite.on('mouseup', this.onMouseUp.bind(this));
        this.screenSprite.on('mouseupoutside', this.onMouseUp.bind(this));
        this.screenSprite.on('touchend', this.onMouseUp.bind(this));
        this.screenSprite.on('touchendoutside', this.onMouseUp.bind(this));

        this.screenSprite.on('mousemove', this.onMouseMove.bind(this));
        this.screenSprite.on('touchmove', this.onMouseMove.bind(this));

        this.screenSprite.on('mousewheel', this.onMouseWheel.bind(this));
*/


        PIXI.utils.clearTextureCache()
        PIXI.utils.destroyTextureCache()

        const loader = this.app.loader;
        const img = PIXI.utils.TextureCache['assets/game/base.json_image'];

        if (img == undefined) {
            loader.add("assets/game/base.json")
            loader.add("assets/game/tiles.json")
            loader.load((_, __) => {
                this.setup();
            })
        } else {
            this.setup();
        }
    }

    private destroy() {
        this.destroyed = true
        this.app.destroy(false, {
            children: true,
        });
        for (let gridsKey in this.grids) {
            this.grids[gridsKey].destroy()
        }
        this.mapGrids.destroy({children: true})
        this.objectsContainer.destroy({children: true})
        this.touchCurrent = {}
    }

    private setup() {
        const gameData = GameClient.data;

        // идем по всем полученным кусочкам карты
        for (let key in gameData.map) {
            let s = key.split("_");
            let x: number = +s[0];
            let y: number = +s[1];
            this.addGrid(x, y)
        }

        // пройдем по всем уже полученным объектам и добавим их в игровой мир
        for (let key in gameData.objects) {
            this.onObjectAdd(gameData.objects[key])
        }

        // this.myStatus = new PlayerStatus(this)

        // обновим положение карты
        this.updateMapScalePos();
    }

    /**
     * добавить ранее полученный от сервера грид в игру
     */
    public addGrid(x: number, y: number) {
        const gameData = GameClient.data;
        let k = x + "_" + y
        // такой грид уже есть и создан
        if (this.grids[k] !== undefined) {
            let g = this.grids[k]
            // сделаем его видимым
            if (!g.visible) {
                g.visible = true
            }
            // а если еще и изменился - перестроим его
            if (gameData.map[k].isChanged) {
                // TODO если изменился тайл в нашем гриде и он на границе.
                //  надо обновить и соседние гриды. т.к. там возможно перекрытие тайлов
                g.rebuild()
            }
        } else {
            // такого грида еще нет - надо создать
            this.grids[k] = new Grid(this.mapGrids, x, y)

            // зачистим старые гриды, которые давно уже не видели
            for (let gridsKey in this.grids) {
                const grid = this.grids[gridsKey];
                const dist = Math.sqrt(Math.pow(gameData.playerObject.x - grid.absoluteX, 2) + Math.pow(gameData.playerObject.y - grid.absoluteY, 2))
                // дистанция от игрока на которой начнем удалять гриды иэ кэша
                const limit = 5 * Tile.FULL_GRID_SIZE
                if (!grid.visible && dist > limit) {
                    grid.destroy()
                    delete this.grids[gridsKey]
                    console.warn("old grid delete ", gridsKey)
                }
            }
        }
        gameData.map[k].isChanged = false
    }

    /**
     * удалить грид из игры (скрыть его до поры до времени)
     */
    public deleteGrid(x: number, y: number) {
        for (let gridsKey in this.grids) {
            if (this.grids[gridsKey].x == x && this.grids[gridsKey].y == y) {
                console.log("delete grid", gridsKey)
                this.grids[gridsKey].visible = false
                break
            }
        }
    }

    public updateMapScalePos() {
        const px = GameClient.data.playerObject.x;
        const py = GameClient.data.playerObject.y;

        const sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
        const sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;

        // центр экрана с учетом отступа перетаскиванием
        const offx = this.app.renderer.width / 2 + this.offset.x;
        const offy = this.app.renderer.height / 2 + this.offset.y;

        this.mapGrids.x = offx - sx * this.scale;
        this.mapGrids.y = offy - sy * this.scale;
        this.objectsContainer.x = offx - sx * this.scale;
        this.objectsContainer.y = offy - sy * this.scale;

        this.mapGrids.scale.x = this.scale;
        this.mapGrids.scale.y = this.scale;
        this.objectsContainer.scale.x = this.scale;
        this.objectsContainer.scale.y = this.scale;

        if (this.actionProgress !== undefined) {
            this.actionProgress.sprite.x = this.app.renderer.width / 2
            this.actionProgress.sprite.y = this.app.renderer.height / 2
        }
    }

    private update() {
        if (this.destroyed) return

        // delta time in seconds
        // важно. берем elapsedMS т.к. у нас сервер управляет движением. и нам надо абсолютное время ни от чего не зависящее
        let dt = PIXI.Ticker.shared.elapsedMS / 1000

        // изменились размеры окна
        if (this.app.renderer.width !== this.screenSprite.width || this.app.renderer.height !== this.screenSprite.height) {
            this.onResize()
        }

        // передвигаем все движущиеся объекты
        let cnt = 0
        for (let key in this.movingObjects) {
            let moveController = this.movingObjects[key].moveController
            if (moveController !== undefined) {
                moveController.update(dt)
            }
            cnt++
        }
        if (cnt > 0) {
            this.objectsContainer.sortChildren()
        }
    }

    private onResize() {
        this.app.renderer.resize(window.innerWidth, window.innerHeight)

        this.screenSprite.width = this.app.renderer.width
        this.screenSprite.height = this.app.renderer.height
        this.updateMapScalePos()
    }

    public onObjectAdd(obj: GameObject) {
        if (obj.view !== undefined) {
            obj.view.destroy()
        }

        obj.view = new ObjectView(obj)
        for (let i = 0; i < obj.view.view.length; i++) {
            this.objectsContainer.addChild(obj.view.view[i])
        }
    }

    public onObjectMoved(obj: GameObject) {
        obj.view?.onMoved()
    }

    public onObjectDelete(obj: GameObject) {
        obj.view?.destroy()
    }

    public onObjectChange(obj: GameObject) {
        // TODO onObjectChange
    }

    /**
     * перевести экранные координаты в игровые
     */
    public coordScreen2Game(p: Point): Point {
        p.dec(this.offset);

        let px = GameClient.data.playerObject.x;
        let py = GameClient.data.playerObject.y;

        console.log("player pos " + px + " " + py)

        let screenWidthHalf = this.app.renderer.width / 2;
        let screenHeightHalf = this.app.renderer.height / 2;
        p.decValue(screenWidthHalf, screenHeightHalf).div(this.scale);

        return new Point(
            p.y / Tile.TEXTURE_HEIGHT + p.x / Tile.TEXTURE_WIDTH,
            p.y / Tile.TEXTURE_HEIGHT - p.x / Tile.TEXTURE_WIDTH)
            .mul(Tile.TILE_SIZE)
            .incValue(px, py)
            .round();
    }

    public static coordGame2Screen(x: number, y: number): Coord {
        let px = x / Tile.TILE_SIZE;
        let py = y / Tile.TILE_SIZE;
        return [px * Tile.TILE_WIDTH_HALF - py * Tile.TILE_WIDTH_HALF, px * Tile.TILE_HEIGHT_HALF + py * Tile.TILE_HEIGHT_HALF];
    }

    public coordGame2ScreenAbs(x: number, y: number): Coord {
        const px = GameClient.data.playerObject.x;
        const py = GameClient.data.playerObject.y;

        const sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
        const sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;


        let cx = x / Tile.TILE_SIZE;
        let cy = y / Tile.TILE_SIZE;
        let ax = cx * Tile.TILE_WIDTH_HALF - cy * Tile.TILE_WIDTH_HALF
        let ay = cx * Tile.TILE_HEIGHT_HALF + cy * Tile.TILE_HEIGHT_HALF


        // центр экрана с учетом отступа перетаскиванием
        let offx = this.app.renderer.width / 2 + this.offset.x;
        let offy = this.app.renderer.height / 2 + this.offset.y;
        console.log("coord", ax, ay)
        console.log("offx=" + offx + " offy=" + offy)
        return [offx - (sx - ax) * this.scale, offy - (sy - ay) * this.scale]
    }
}
