import * as PIXI from 'pixi.js';
import GameClient from "../net/GameClient";
import Grid from "./Grid";
import Tile from "./Tile";
import GameObject from "./GameObject";
import Coord from "./Coord";
import ObjectView from "./ObjectView";
import Point from "./Point";
import ActionProgress from "./ActionProgress";
import {ContextMenuData} from "../net/packets";
import ContextMenu from "./ContextMenu";
import {mobileAndTabletCheck} from "../utils/mobileCheck";
import {getKeyFlags} from "../utils/keyboard";

export default class Render {

    public static instance?: Render = undefined;

    private canvas: HTMLCanvasElement;

    /**
     * игра запущена на мобилке?
     */
    public static isMobile: boolean = mobileAndTabletCheck()

    /**
     * PIXI application
     */
    private readonly app: PIXI.Application;

    /**
     * не реагируем на апдейт после уничтожения рендера
     * апдейт биндится в pixi ticker
     */
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

    /**
     * текущее открытое контекстное меню
     */
    private contextMenu ?: ContextMenu

    public static start(): Promise<undefined> {
        console.warn("pixi start");
        this.instance = new Render()
        // сразу запустим загрузку ресурсов и вернем промис
        return this.instance.load()
    }

    public static stop() {
        console.warn("pixi stop");
        Render.instance?.destroy();
        Render.instance = undefined;
    }

    private constructor() {
        // создаем канвас для рендера
        this.canvas = document.createElement('canvas');
        this.canvas.id = "game"
        // но не показываем его, покажем после загрузки всех ресурсов
        this.canvas.style.display = "none"
        // добавим его в дом дерево
        document.body.appendChild(this.canvas);
        this.initCanvasHandlers()


        // сглаживание пикселей при масштабировании
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

        this.app = new PIXI.Application({
            width: window.innerWidth,
            height: window.innerHeight,
            view: this.canvas,
            autoDensity: true,
            preserveDrawingBuffer: true,
            powerPreference: 'high-performance',
            antialias: false,
            backgroundColor: 0x333333
        });

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

        // TODO удалить. onMouseWheel биндим в initCanvasHandlers
        // this.screenSprite.on('mousewheel', this.onMouseWheel.bind(this));
    }

    /**
     * загрузить необходимые для старта атласы
     * @private
     */
    private load(): Promise<undefined> {
        PIXI.utils.clearTextureCache()
        PIXI.utils.destroyTextureCache()

        const loader = this.app.loader;

        const render = this;
        return new Promise(function (resolve, reject) {
            loader.onError.add((e) => {
                reject(e)
            })

            // грузим нужные нам атласы
            loader.add("assets/game/base.json")
            loader.add("assets/game/tiles.json")

            loader.load((_, __) => {
                render.setup()
                resolve(undefined)
            })
        });
    }

    private destroy() {
        const canvas = this.app.view
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

        // грохнем ранее созданный канвас
        document.body.removeChild(canvas)
    }

    private setup() {
        this.canvas.style.display = "block"

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
                const playerObject = gameData.playerObject;
                if (playerObject !== undefined) {
                    const dist = Math.sqrt(Math.pow(playerObject.x - grid.absoluteX, 2) + Math.pow(playerObject.y - grid.absoluteY, 2))
                    // дистанция от игрока на которой начнем удалять гриды иэ кэша
                    const limit = 5 * Tile.FULL_GRID_SIZE
                    if (!grid.visible && dist > limit) {
                        grid.destroy()
                        delete this.grids[gridsKey]
                        console.warn("old grid delete ", gridsKey)
                    }
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
        const {sx, sy} = Render.getPlayerCoord();

        // центр экрана с учетом отступа перетаскиванием
        const offsetX = this.app.renderer.width / 2 + this.offset.x;
        const offsetY = this.app.renderer.height / 2 + this.offset.y;

        this.mapGrids.x = offsetX - sx * this.scale;
        this.mapGrids.y = offsetY - sy * this.scale;
        this.objectsContainer.x = offsetX - sx * this.scale;
        this.objectsContainer.y = offsetY - sy * this.scale;

        this.mapGrids.scale.x = this.scale;
        this.mapGrids.scale.y = this.scale;
        this.objectsContainer.scale.x = this.scale;
        this.objectsContainer.scale.y = this.scale;

        if (this.actionProgress !== undefined) {
            this.actionProgress.sprite.x = this.app.renderer.width / 2
            this.actionProgress.sprite.y = this.app.renderer.height / 2
        }
    }

    private static getPlayerCoord(): { sx: number, sy: number } {
        const playerObject = GameClient.data.playerObject;
        if (playerObject !== undefined) {
            const px = playerObject.x;
            const py = playerObject.y;

            const sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF;
            const sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF;
            return {sx, sy};
        } else {
            console.warn("no player object")
            return {sx: 0, sy: 0};
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

    private updateScale() {
        if (this.scale < 0.05) this.scale = 0.05
        this.updateMapScalePos();
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
        const {sx, sy} = Render.getPlayerCoord();

        const cx = x / Tile.TILE_SIZE;
        const cy = y / Tile.TILE_SIZE;
        const ax = cx * Tile.TILE_WIDTH_HALF - cy * Tile.TILE_WIDTH_HALF
        const ay = cx * Tile.TILE_HEIGHT_HALF + cy * Tile.TILE_HEIGHT_HALF


        // центр экрана с учетом отступа перетаскиванием
        const offsetX = this.app.renderer.width / 2 + this.offset.x;
        const offsetY = this.app.renderer.height / 2 + this.offset.y;
        console.log("coord", ax, ay)
        console.log("offx=" + offsetX + " offy=" + offsetY)
        return [offsetX - (sx - ax) * this.scale, offsetY - (sy - ay) * this.scale]
    }

    private onMouseDown(e: PIXI.InteractionEvent) {
        this.touchCurrent[e.data.identifier] = new Point(e.data.global)

        // двигаем карту если средняя кнопка мыши или мобильное устройство
        if (e.data.button == 1 || Render.isMobile) {
            this.dragStart = new Point(e.data.global).round();
            this.dragOffset = new Point(this.offset);
            console.log('onMouseDown ' + this.dragStart.toString());
        } else if (e.data.button == 0 && !Render.isMobile) {
            const p = new Point(e.data.global).round();
            this.lastMoveCoord = new Point(e.data.global).round();
            console.log("lastMoveCoord!!!!!!!", this.lastMoveCoord)
            // это просто клик. без сдвига карты
            const cp = this.coordScreen2Game(p);

            console.log("mapclick " + cp.toString());
            GameClient.remoteCall("mapclick", {
                b: e.data.button,
                f: getKeyFlags(e),
                x: cp.x,
                y: cp.y
            })

            // запускаем таймер который периодически шлет клики по карте на сервер в текущие экранные координаты
            this.continuousMovingTimer = window.setInterval(() => {
                if (this.lastMoveCoord !== undefined) {
                    console.log("lastMoveCoord", this.lastMoveCoord)
                    const cp = this.coordScreen2Game(this.lastMoveCoord.clone());

                    console.log("mapclick " + cp.toString());
                    GameClient.remoteCall("mapclick", {
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
                GameClient.remoteCall("mapclick", {
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
            if (this.continuousMovingTimer !== -1) {
                clearInterval(this.continuousMovingTimer)
            }
            this.lastMoveCoord = undefined
            this.continuousMovingTimer = -1
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

    private onMouseRightClick(e: MouseEvent) {

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
     * навешиваем на канвас обработчики
     */
    private initCanvasHandlers() {
        // ловим прокрутку страницы и делаем скейл на основе этого
        this.canvas.addEventListener('wheel', (e: WheelEvent) => {
            e.preventDefault();
            this.onMouseWheel(-e.deltaY);
        });

        // ловим правый клик за счет вызоыва context menu
        this.canvas.addEventListener('contextmenu', (e: Event) => {
            e.preventDefault();
            if (e instanceof MouseEvent) {
                console.log("right click " + e.x + " " + e.y);
                console.log(e.button + " alt=" + e.altKey + " shift=" + e.shiftKey + " meta=" + e.metaKey);
                this.onMouseRightClick(e);
            }
        });

        // обработчик resize
        let resizeTimeout: any = undefined;
        window.addEventListener('resize', () => {
            if (resizeTimeout == undefined) {
                resizeTimeout = setTimeout(() => {
                    resizeTimeout = undefined;
                    console.log("resize");
                    this.onResize();
                }, 333);
            }
        });

        // переворот экрана
        window.addEventListener("orientationchange", () => {
            this.onResize();
        });

        document.addEventListener('keydown', (e: Event) => {
            if (e.target == document.body && e instanceof KeyboardEvent) {
                this.onKeyDown(e)
            }
        })
    }
}
