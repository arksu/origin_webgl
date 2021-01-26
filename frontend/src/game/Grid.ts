import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import Client from "@/net/Client";

export default class Grid {

    public containers: PIXI.Container[] = [];

    // 2 4 5
    private static readonly DIVIDER = 4

    private readonly CHUNK_SIZE = Tile.GRID_SIZE / Grid.DIVIDER

    private app: PIXI.Application

    public readonly x: number
    public readonly y: number

    private spriteTextureNames: string[] = []
    private tiles: PIXI.Sprite[] = [];

    constructor(app: PIXI.Application, x: number, y: number) {
        this.app = app;
        this.x = x;
        this.y = y;

        this.makeChunks()

        // агрессивное кэширование гридов карты, иначе каждый раз все рендерится потайлово
        for (let i = 0; i < this.containers.length; i++) {
            this.containers[i].cacheAsBitmap = true
        }
    }

    private makeChunks() {
        // создаем чанки грида 4x4
        for (let cx = 0; cx < Grid.DIVIDER; cx++) {
            for (let cy = 0; cy < Grid.DIVIDER; cy++) {
                let idx = cx + cy * Grid.DIVIDER
                let container = this.makeChunk(cx, cy, idx)
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
        this.containers = []
    }

    private makeChunk(cx: number, cy: number, idx: number): PIXI.Container {
        let container = new PIXI.Container();
        // координаты грида с учетом чанка
        let x = this.x + cx / Grid.DIVIDER
        let y = this.y + cy / Grid.DIVIDER
        // координаты грида ставим в абсолютные мировые в тайлах
        container.x = x * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - y * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - Tile.TILE_WIDTH_HALF;
        container.y = x * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE + y * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE;

        this.makeTiles(container, cx, cy, idx);
        container.calculateBounds()
        // console.log("grid screen x=" + container.x + " y=" + container.y + " w=" + container.width + " h=" + container.height)

        return container;
    }

    private makeTiles(container: PIXI.Container, cx: number, cy: number, chunk: number) {
        let key = this.x + "_" + this.y;
        let data = Client.instance.map[key];

        for (let tx = 0; tx < this.CHUNK_SIZE; tx++) {
            for (let ty = 0; ty < this.CHUNK_SIZE; ty++) {

                let x = cx * this.CHUNK_SIZE + tx;
                let y = cy * this.CHUNK_SIZE + ty;
                const idx = y * Tile.GRID_SIZE + x

                const tn = Tile.getGroundTexture(data[y * Tile.GRID_SIZE + x])
                this.spriteTextureNames[idx] = tn

                let path = tn
                if (path.includes(".")) path = "assets/" + path

                let spr = PIXI.Sprite.from(path);

                // spr.tint = 500000 * chunk;

                container.addChild(spr)

                spr.x = tx * Tile.TILE_WIDTH_HALF - ty * Tile.TILE_WIDTH_HALF;
                spr.y = tx * Tile.TILE_HEIGHT_HALF + ty * Tile.TILE_HEIGHT_HALF;

                this.tiles[idx] = spr;
            }
        }
    }

    public onFileChange(fn: string) {
        let path = "assets/" + fn + "?" + (+new Date())

        for (let i = 0; i < this.containers.length; i++) {
            this.containers[i].cacheAsBitmap = false
        }
        PIXI.Texture.fromURL(path).then(() => {

            for (let i = 0; i < this.tiles.length; i++) {
                let spr = this.tiles[i]

                if (this.spriteTextureNames[i] == fn) {
                    spr.texture = PIXI.Texture.from(path)
                }
            }

            for (let i = 0; i < this.containers.length; i++) {
                this.containers[i].cacheAsBitmap = true
            }
        })
    }
}
