import * as PIXI from 'pixi.js';
import {GameObject} from "@/game/GameObject";
import Net from "@/net/Net";
import objects from "./objects.json"
import Game from "@/game/Game";
import {Coord, Layer, Resource} from "@/utils/Util";
import Point from "@/utils/Point";

/**
 * внешнее представлениен объекта в игре
 */
export default class ObjectView {
    obj: GameObject
    view: PIXI.Sprite[] = []
    isDestroyed: boolean = false

    private isTouched: boolean = false
    private touchTimer: number = -1
    private wasRightClick: boolean = false

    private readonly res: Resource
    private layersOffset: Coord[] = []

    constructor(obj: GameObject) {
        this.obj = obj

        this.res = this.getResource(obj.r)
        if (this.res == undefined) {
            this.res = this.getResource("unknown")
        }
        this.makeLayers()
        this.onMoved()
    }

    /**
     * получить ресурс объекта из пути
     */
    private getResource(r: string): Resource {
        let strings = r.split("/");

        // берем первый уровень вложенности
        // @ts-ignore
        let res = <Resource>objects[strings[0]]
        if (res == undefined) return res

        // идем по всем последующим
        for (let i = 1; i < strings.length; i++) {
            // @ts-ignore
            res = res[strings[i]]
            if (res == undefined) return res
        }
        return res
    }

    public makeLayers() {
        // идем по слоям
        for (let i = 0; i < this.res.layers.length; i++) {
            this.addLayer(this.res.layers[i])
        }
        this.isDestroyed = false
    }

    private addLayer(l: Layer) {
        let path = l.img
        // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
        if (path.includes(".")) path = "assets/" + path

        // если есть оффсет надо его проставить
        if (l.offset != undefined) {
            this.layersOffset.push(l.offset)
        } else {
            this.layersOffset.push([0, 0])
        }
        // создаем спрайт для каждого слоя
        let spr = PIXI.Sprite.from(path)
        if (l.interactive) this.setInteractive(spr)
        if (l.shadow) {
            spr.zIndex = -1
        }
        this.view.push(spr)
    }

    public destroy() {
        if (!this.isDestroyed) {
            for (let i = 0; i < this.view.length; i++) {
                this.view[i].destroy();
            }
            this.isDestroyed = true
        }
    }

    public onMoved() {
        let coord = Game.coordGame2Screen(this.obj.x, this.obj.y)

        for (let i = 0; i < this.view.length; i++) {
            // оффсет слоя
            this.view[i].x = coord[0] + this.layersOffset[i][0]
            this.view[i].y = coord[1] + this.layersOffset[i][1]

            // добавим оффсет для объекта
            if (this.res.offset != undefined) {
                this.view[i].x -= this.res.offset[0]
                this.view[i].y -= this.res.offset[1]
            }
            // z = y
            let z = coord[1]
            if (this.res.layers[i].shadow) {
                z = -1
            }
            this.view[i].zIndex = z
        }
    }

    /**
     * сервер уведомил о том что изменился файл в ассетах
     * @param f путь до текстуры
     */
    public onAssetsChanged(f: string) {
        // идем по слоям
        for (let i = 0; i < this.res.layers.length; i++) {
            if (this.res.layers[i].img == f) {
                let l = this.res.layers[i]
                let path = l.img
                // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
                if (path.includes(".")) path = "assets/" + path + "?" + (+new Date())

                this.view[i].texture = PIXI.Texture.from(path)
            }
        }
    }

    private setInteractive(target: PIXI.Sprite | PIXI.Container) {
        target.interactive = true
        target.on("rightclick", () => {
            this.onRightClick()
        })
        target.on("touchstart", () => {
            this.isTouched = true
            this.touchTimer = setTimeout(() => {
                if (this.isTouched) {
                    this.isTouched = false
                    // укажем что был правый клик (сработал)
                    this.wasRightClick = true
                    console.log("rightclick")
                    this.onRightClick()

                    clearTimeout(this.touchTimer)
                    this.touchTimer = -1
                }
            }, 800)
        })
        target.on("touchend", (e: PIXI.InteractionEvent) => {
            this.isTouched = false
            if (this.touchTimer != -1) {
                clearTimeout(this.touchTimer)
                this.touchTimer = -1
            }
            // только если не сработал правый клик - выполним основной клик
            if (!this.wasRightClick) {
                this.onClick(e)
            }
            // и в любом случае затрем флаг правого клика
            this.wasRightClick = false
        })
        target.on("touchendoutside", () => {
            this.isTouched = false
            if (this.touchTimer != -1) {
                clearTimeout(this.touchTimer)
                this.touchTimer = -1
            }
        })
        target.on("mouseup", (e: PIXI.InteractionEvent) => {
            this.onClick(e)
        })
    }

    private onClick(e: PIXI.InteractionEvent) {
        // screen point coord
        const p = new Point(e.data.global).round();
        if (Game.instance !== undefined) {
            // вычислим игровые координаты куда тыкнула мышь
            // их тоже отправим на сервер
            let cp = Game.instance?.coordScreen2Game(p);

            Net.remoteCall("objclick", {
                id: this.obj.id,
                x: cp.x,
                y: cp.y
            })
        }
    }

    private onRightClick() {
        Net.remoteCall("objrclick", {
            id: this.obj.id
        })
    }
}