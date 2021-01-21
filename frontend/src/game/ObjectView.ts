import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import {GameObject} from "@/game/GameObject";

/**
 * внешнее представлениен объекта в игре
 */
export default class ObjectView {
    obj: GameObject
    view: PIXI.Sprite | PIXI.Container
    isDestroyed: boolean = false

    private isTouched: boolean = false
    private touchTimer: number = -1

    constructor(obj: GameObject) {
        this.obj = obj

        if (obj.c == "Player") {
            this.view = PIXI.Sprite.from("man")
        } else if (obj.c == "StaticObject") {
            switch (obj.t) {
                case 23 :
                    this.view = PIXI.Sprite.from("box")
                    this.setInteractive()
                    break
                case 1 :
                    this.view = PIXI.Sprite.from("tree1")
                    break
                default :
                    this.view = PIXI.Sprite.from("question")
                    break
            }
        } else {
            throw Error("unknown class object " + obj.c)
        }
        this.onMoved()
    }

    public destroy() {
        if (!this.isDestroyed) {
            this.view.destroy();
            this.isDestroyed = true
        }
    }

    public onMoved() {
        let px = this.obj.x / Tile.TILE_SIZE;
        let py = this.obj.y / Tile.TILE_SIZE;

        this.view.x = px * Tile.TILE_WIDTH_HALF - py * Tile.TILE_WIDTH_HALF;
        this.view.y = px * Tile.TILE_HEIGHT_HALF + py * Tile.TILE_HEIGHT_HALF;
        this.view.x -= 17
        this.view.y -= 57
    }

    private setInteractive() {
        this.view.interactive = true
        this.view.on("rightclick", () => {
            this.onRightClick()
        })
        this.view.on("touchstart", () => {
            this.isTouched = true
            this.touchTimer = setTimeout(() => {
                if (this.isTouched) {
                    this.isTouched = false
                    console.log("rightclick")
                    this.onRightClick()

                    clearTimeout(this.touchTimer)
                    this.touchTimer = -1
                }
            }, 1000)
        })
        this.view.on("touchend", () => {
            this.isTouched = false
            if (this.touchTimer != -1) {
                clearTimeout(this.touchTimer)
                this.touchTimer = -1
            }
            this.onClick()
        })
        this.view.on("touchendoutside", () => {
            this.isTouched = false
            if (this.touchTimer != -1) {
                clearTimeout(this.touchTimer)
                this.touchTimer = -1
            }
        })
        this.view.on("mouseup", () => {
            this.onClick()
        })
    }

    private onClick() {

    }

    private onRightClick() {
        this.view.visible = false
    }
}