import * as PIXI from 'pixi.js';

import Client from "@/net/Client";
import Grid from "@/game/Grid";
import Tile from "@/game/Tile";
import Net from "@/net/Net";
import Point from '@/utils/Point';
import {GameObject} from "@/game/GameObject";
import ObjectView from "@/game/ObjectView";
import {Coord, getKeyFlags, mobileAndTabletCheck} from "@/utils/Util";
import ContextMenu from "@/game/ContextMenu";
import {ActionProgressData, ContextMenuData} from "@/net/Packets";
import ActionProgress from "@/game/ActionProgress";
import PlayerStatus from "@/game/PlayerStatus";

/**
 * основная игровая логика (графика и тд)
 */
export default class Game {

    private static readonly canvas: HTMLCanvasElement = <HTMLCanvasElement>document.getElementById("game");
    // private static readonly appDiv: HTMLElement = <HTMLElement>document.getElementById("app");

    public static instance?: Game = undefined;

    /**
     * игра запущена на мобилке?
     */
    public static isMobile: boolean = mobileAndTabletCheck()

    /**
     * PIXI app
     */
    readonly app: PIXI.Application;

    private destroyed: boolean = false

    /**
     * контейнер в котором храним контейнеры с гридами и тайлами
     * их координаты внутри абсолютные мировые экранные
     */
    private readonly mapGrids: PIXI.Container;

    public readonly objectsContainer: PIXI.Container

    /**
     * загруженные гриды
     */
    private grids: { [key: string]: Grid } = {};

    /**
     * невидимый спрайт на весь экран для обработки кликов мыши
     */
    private readonly screenSprite: PIXI.Sprite;

    /**
     * масштаб карты и игровой графики
     */
    private scale: number = 1;

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
    private continousMovingTimer: number = -1

    /**
     * последние экранные координаты при нажатой ЛКМ
     */
    private lastMoveCoord ?: Point

    /**
     * список объектов которые в данный момент движутся
     * апдейтим их позицию каждый тик через move controller
     */
    public movingObjects: { [key: number]: GameObject } = {}

    /**
     * текущее открытое контекстное меню
     */
    private contextMenu ?: ContextMenu

    /**
     * прогресс текущего действия (отображаем по середине экрана)
     */
    private actionProgress ?: ActionProgress

    /**
     * отображение хп, стамины и тд
     */
    private myStatus?: PlayerStatus

    public static start() {
        console.warn("pixi start");

        this.instance = new Game();

        this.canvas.style.display = "block";
    }

    public static stop() {
        console.warn("pixi stop");

        Game.instance?.destroy();
        Game.instance = undefined;
        Client.instance.clear();

        this.canvas.style.display = "none";
    }

    constructor() {
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

        this.app = new PIXI.Application({
            width: window.innerWidth,
            height: window.innerHeight,
            view: Game.canvas,
            autoDensity: true,
            preserveDrawingBuffer: true,
            powerPreference: 'high-performance',
            // resizeTo: window,
            antialias: false,
            // clearBeforeRender: true,
            backgroundColor: 0x333333
        });

        // this.app.renderer.resize(Game.canvas.width, Game.canvas.height);
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

        this.screenSprite.on('mousedown', this.onMouseDown.bind(this));
        this.screenSprite.on('touchstart', this.onMouseDown.bind(this));

        this.screenSprite.on('mouseup', this.onMouseUp.bind(this));
        this.screenSprite.on('mouseupoutside', this.onMouseUp.bind(this));
        this.screenSprite.on('touchend', this.onMouseUp.bind(this));
        this.screenSprite.on('touchendoutside', this.onMouseUp.bind(this));

        this.screenSprite.on('mousemove', this.onMouseMove.bind(this));
        this.screenSprite.on('touchmove', this.onMouseMove.bind(this));

        this.screenSprite.on('mousewheel', this.onMouseWheel.bind(this));


        PIXI.utils.destroyTextureCache()
        PIXI.utils.clearTextureCache()

        const loader = this.app.loader;
        const img = PIXI.utils.TextureCache['assets/base.json_image'];

        if (img == undefined) {
            loader.add("assets/base.json")
            loader.add("assets/tiles.json")
            loader.load((_, __) => {
                this.setup();
            })
        } else {
            this.setup();
        }
    }

    private destroy() {
        console.warn("Game destroy")
        this.app.destroy(false, {
            children: true,
            // texture: true,
            // baseTexture: true
        });

        for (let gridsKey in this.grids) {
            this.grids[gridsKey].destroy()
        }
        this.mapGrids.destroy({children: true})
        this.objectsContainer.destroy({children: true})
        this.touchCurrent = {}
        this.destroyed = true
    }

    private setup() {
        // идем по всем полученным кусочкам карты
        for (let key in Client.instance.map) {
            let s = key.split("_");
            let x: number = +s[0];
            let y: number = +s[1];
            this.addGrid(x, y)
        }

        // пройдем по всем уже полученным объектам и добавим их в игровой мир
        for (let key in Client.instance.objects) {
            this.onObjectAdd(Client.instance.objects[key])
        }

        this.myStatus = new PlayerStatus(this)

        // обновим положение карты
        this.updateMapScalePos();
    }

    /**
     * добавить ранее полученный от сервера грид в игру
     */
    public addGrid(x: number, y: number) {
        let k = x + "_" + y
        // такой грид уже есть и создан
        if (this.grids[k] !== undefined) {
            let g = this.grids[k]
            // сделаем его видимым
            if (!g.visible) {
                g.visible = true
            }
            // а если еще и изменился - перестроим его
            if (Client.instance.map[k].isChanged) {
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
                const dist = Math.sqrt(Math.pow(Client.instance.playerObject.x - grid.absoluteX, 2) + Math.pow(Client.instance.playerObject.y - grid.absoluteY, 2))
                // дистанция от игрока на которой начнем удалять гриды иэ кэша
                const limit = 5 * Tile.FULL_GRID_SIZE
                if (!grid.visible && dist > limit) {
                    grid.destroy()
                    delete this.grids[gridsKey]
                    console.warn("old grid delete ", gridsKey)
                }
            }
        }
        Client.instance.map[k].isChanged = false

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

    private onMouseDown(e: PIXI.InteractionEvent) {
        this.touchCurrent[e.data.identifier] = new Point(e.data.global)

        // двигаем карту если средняя кнопка мыши или мобильное устройство
        if (e.data.button == 1 || Game.isMobile) {
            this.dragStart = new Point(e.data.global).round();
            this.dragOffset = new Point(this.offset);
            console.log('onMouseDown ' + this.dragStart.toString());
        } else if (e.data.button == 0 && !Game.isMobile) {
            const p = new Point(e.data.global).round();
            this.lastMoveCoord = new Point(e.data.global).round();
            console.log("lastMoveCoord!!!!!!!", this.lastMoveCoord)
            // это просто клик. без сдвига карты
            const cp = this.coordScreen2Game(p);

            console.log("mapclick " + cp.toString());
            Net.remoteCall("mapclick", {
                b: e.data.button,
                f: getKeyFlags(e),
                x: cp.x,
                y: cp.y
            })

            // запускаем таймер который периодически шлет клики по карте на сервер в текущие экранные координаты
            this.continousMovingTimer = setInterval(() => {
                if (this.lastMoveCoord !== undefined) {
                    console.log("lastMoveCoord", this.lastMoveCoord)
                    const cp = this.coordScreen2Game(this.lastMoveCoord.clone());

                    console.log("mapclick " + cp.toString());
                    Net.remoteCall("mapclick", {
                        b: e.data.button,
                        f: getKeyFlags(e),
                        x: cp.x,
                        y: cp.y
                    })
                }
            }, 333)
        }
        this.dragMoved = false;
    }

    private onMouseUp(e: PIXI.InteractionEvent) {
        delete this.touchCurrent[e.data.identifier]

        this.touchLength = -1

        // screen point coord
        const p = new Point(e.data.global).round();

        console.log('onMouseUp ' + p.toString());
        console.log(e)

        // если сдвинули карту при нажатии мыши
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
                    b: e.data.button,
                    f: getKeyFlags(e),
                    x: cp.x,
                    y: cp.y
                })
            }
            this.dragStart = undefined;
            this.dragOffset = undefined;
            this.touchCurrent = {}
        } else {
            // выключим таймер непрерывного движения
            if (this.continousMovingTimer !== -1) {
                clearInterval(this.continousMovingTimer)
            }
            this.lastMoveCoord = undefined
            this.continousMovingTimer = -1
        }
    }

    private onMouseMove(e: PIXI.InteractionEvent) {
        let keys = Object.keys(this.touchCurrent);
        if (keys.length == 2) {
            this.dragStart = undefined;
            this.dragOffset = undefined;

            let current = new Point(e.data.global)
            this.touchCurrent[e.data.identifier] = current

            let cx2 = 0
            let cy2 = 0
            // ищем вторую точку
            for (let key of keys) {
                let k = +key
                if (k !== e.data.identifier) {
                    cx2 = this.touchCurrent[k].x
                    cy2 = this.touchCurrent[k].y
                }
            }

            // touch len
            let tl = Math.sqrt(Math.pow(current.x - cx2, 2) + Math.pow(current.y - cy2, 2))
            if (this.touchLength < 0) {
                this.touchLength = tl
            }
            // на сколько изменилась длина между касаниями
            let dt = tl - this.touchLength
            this.touchLength = tl

            if (this.scale < 1) {
                this.scale += dt * 0.007 * this.scale
            } else {
                this.scale += dt * 0.01
            }
            this.updateScale()
        } else {
            if (this.dragStart !== undefined && this.dragOffset !== undefined) {
                let p = new Point(e.data.global).round();

                p.dec(this.dragStart);

                if (Math.abs(p.x) > 10 || Math.abs(p.y) > 10 || this.dragMoved) {
                    this.dragMoved = true;
                    this.offset.set(this.dragOffset).inc(p);

                    this.updateMapScalePos();
                }
            } else {
                // обновляем текущие экранные координаты
                this.lastMoveCoord = new Point(e.data.global).round();
            }
        }
    }

    private onMouseWheel(delta: number) {
        if (this.scale < 1) {
            this.scale += delta / (1000) * (this.scale * 0.5);
        } else {
            this.scale += delta / (1000);
        }
        this.updateScale()
    }

    private onKeyDown(e: KeyboardEvent) {
        console.log("onKeyDown:", e.key)
        switch (e.key) {
            case "Enter":
                document.getElementById("inputChat")?.focus();
                break;
            case "Home":
                this.scale = 1
                this.updateScale()
                break;
        }
    }

    private updateScale() {
        if (this.scale < 0.05) this.scale = 0.05
        this.updateMapScalePos();
    }

    private onMouseRightClick(e: MouseEvent) {

    }

    public updateMapScalePos() {
        const px = Client.instance.playerObject.x;
        const py = Client.instance.playerObject.y;

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

    /**
     * перевести экранные координаты в игровые
     */
    public coordScreen2Game(p: Point): Point {
        p.dec(this.offset);

        let px = Client.instance.playerObject.x;
        let py = Client.instance.playerObject.y;

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
        const px = Client.instance.playerObject.x;
        const py = Client.instance.playerObject.y;

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

    public onFileChange(fn: string) {
        for (let gridsKey in this.grids) {
            this.grids[gridsKey].onFileChange(fn)
        }
    }

    /**
     * создать и отобразить контекстное меню которое прислал сервер
     */
    public makeContextMenu(cm?: ContextMenuData) {
        console.log(cm)
        if (this.contextMenu !== undefined) {
            this.contextMenu.destroy()
        }
        if (cm !== undefined) {
            this.contextMenu = new ContextMenu(cm)
            this.app.stage.addChild(this.contextMenu.container)
            let sc = this.coordGame2ScreenAbs(cm.obj.x, cm.obj.y)
            this.contextMenu.container.x = sc[0]
            this.contextMenu.container.y = sc[1]
            console.log(this.contextMenu.container)
        }
    }

    /**
     * установить прогресс совершаемого действия
     * @param ap данные от сервера
     */
    public setActionProgress(ap: ActionProgressData) {
        if (ap.t > 0) { // total
            if (this.actionProgress == undefined) {
                this.actionProgress = new ActionProgress(ap)
                this.app.stage.addChild(this.actionProgress.sprite)
                this.actionProgress.sprite.x = this.app.renderer.width / 2
                this.actionProgress.sprite.y = this.app.renderer.height / 2
            }
            this.actionProgress.apply(ap)
        } else {
            // сервер говорит что действие закончилось и надо убрать его с экрана
            this.actionProgress?.destroy()
            this.actionProgress = undefined
        }
    }

    public onMyStatusUpdate() {
        this.myStatus?.update()
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
                    Game.instance?.onResize();
                }, 333);
            }
        });

        // переворот экрана
        window.addEventListener("orientationchange", () => {
            Game.instance?.onResize();
        });

        document.addEventListener('keydown', (e: Event) => {
            if (e.target == document.body && e instanceof KeyboardEvent) {
                Game.instance?.onKeyDown(e)
            }
        })
    }
}
