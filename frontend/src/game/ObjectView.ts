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

    constructor(obj: GameObject) {
        this.obj = obj

        if (obj.t == "Player") {
            this.view = PIXI.Sprite.from("man")
        } else {
            throw Error("unknown type object "+obj.t)
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
}