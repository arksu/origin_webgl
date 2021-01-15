import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import Client from "@/net/Client";

export default class Grid {

    public containers: PIXI.Container[] = [];

    private readonly CHUNK_SIZE = Tile.GRID_SIZE / 2

    private app: PIXI.Application

    public readonly x: number
    public readonly y: number

    private tiles: PIXI.Sprite[] = [];

    constructor(app: PIXI.Application, x: number, y: number) {
        this.app = app;
        this.x = x;
        this.y = y;

        // создаем чанки грида 4x4
        for (let cx = 0; cx < 2; cx++) {
            for (let cy = 0; cy < 2; cy++) {
                let container = this.makeChunk(cx, cy)
                // container.visible = false
                this.containers.push(container)
            }
        }
    }

    public destroy() {
        for (let container of this.containers) {
            container.destroy({
                children: true
            })
        }
    }

    private makeChunk(cx: number, cy: number): PIXI.Container {
        let container = new PIXI.Container();
        // координаты грида с учетом чанка
        let x = this.x + cx * 0.5
        let y = this.y + cy * 0.5
        // координаты грида ставим в абсолютные мировые в тайлах
        container.x = x * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - y * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - Tile.TILE_WIDTH_HALF;
        container.y = x * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE + y * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE;

        this.makeTiles(container, cx, cy);
        container.calculateBounds()
        // console.log("grid screen x=" + container.x + " y=" + container.y + " w=" + container.width + " h=" + container.height)

        // агрессивное кэширование гридов карты, иначе каждый раз все рендерится потайлово
        container.cacheAsBitmap = true
        return container;
    }

    private makeTiles(container: PIXI.Container, cx: number, cy: number) {
        let key = this.x + "_" + this.y;
        let data = Client.instance.map[key];

        for (let tx = 0; tx < this.CHUNK_SIZE; tx++) {
            for (let ty = 0; ty < this.CHUNK_SIZE; ty++) {

                let x = cx * this.CHUNK_SIZE + tx;
                let y = cy * this.CHUNK_SIZE + ty;

                let tn = Tile.getTextureName(data[y * Tile.GRID_SIZE + x], x, y)

                let s = PIXI.Sprite.from(tn);
                s.roundPixels = false;
                // s.tint = 50000 * (this.x % 2 + this.y % 2);

                container.addChild(s)

                s.x = tx * Tile.TILE_WIDTH_HALF - ty * Tile.TILE_WIDTH_HALF;
                s.y = tx * Tile.TILE_HEIGHT_HALF + ty * Tile.TILE_HEIGHT_HALF;

                this.tiles[y * Tile.GRID_SIZE + x] = s;
            }
        }
    }
}
