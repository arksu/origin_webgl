import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import Client from "@/net/Client";
import {getRandomByCoord} from "@/utils/Util";

export default class Grid {

    public containers: PIXI.Container[] = [];

    // 2 4 5
    private static readonly DIVIDER = 4

    /**
     * размер одной стороны чанка
     */
    private readonly CHUNK_SIZE = Tile.GRID_SIZE / Grid.DIVIDER

    private static readonly bx = [0, 1, 1, 2]
    private static readonly by = [1, 0, 2, 1]
    private static readonly cx = [0, 0, 2, 2]
    private static readonly cy = [0, 2, 2, 0]

    private app: PIXI.Application

    public readonly x: number
    public readonly y: number
    public readonly key: string

    private spriteTextureNames: string[] = []
    private tiles: PIXI.Sprite[] = [];

    constructor(app: PIXI.Application, x: number, y: number) {
        this.app = app;
        this.x = x;
        this.y = y;
        this.key = this.x + "_" + this.y

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
        const data = Client.instance.map[this.key];

        for (let tx = 0; tx < this.CHUNK_SIZE; tx++) {
            for (let ty = 0; ty < this.CHUNK_SIZE; ty++) {

                let x = cx * this.CHUNK_SIZE + tx;
                let y = cy * this.CHUNK_SIZE + ty;
                // индекс в массиве тайлов
                const idx = y * Tile.GRID_SIZE + x

                const tn = Tile.getGroundTexture(data[idx])
                if (tn !== undefined) {
                    this.spriteTextureNames[idx] = tn

                    let path = tn
                    if (path.includes(".")) path = "assets/" + path

                    let spr = PIXI.Sprite.from(path);

                    // spr.tint = 500000 * chunk;

                    const sx = tx * Tile.TILE_WIDTH_HALF - ty * Tile.TILE_WIDTH_HALF
                    const sy = tx * Tile.TILE_HEIGHT_HALF + ty * Tile.TILE_HEIGHT_HALF
                    spr.x = sx;
                    spr.y = sy;
                    container.addChild(spr)

                    this.tiles[idx] = spr;

                    this.makeTransparentTiles(container, data, idx, x, y, sx, sy)
                }

            }
        }
    }

    private makeTransparentTiles(container: PIXI.Container, data: number[], idx: number, x: number, y: number, sx: number, sy: number) {
        let tr: number[][] = []
        // идем по тайлам вокруг целевого
        for (let rx = -1; rx <= 1; rx++) {
            tr[rx + 1] = []
            for (let ry = -1; ry <= 1; ry++) {
                if (rx == 0 && ry == 0) continue

                const dx = x + rx
                const dy = y + ry
                // это тайл еще текущего грида
                let tn = -1
                if (dx >= 0 && dx < Tile.GRID_SIZE && dy >= 0 && dy < Tile.GRID_SIZE) {
                    tn = data[dy * Tile.GRID_SIZE + dx]
                } else {
                    // тайл соседнего грида
                    // смещение тайла который вылез за границы относительно текущего грида
                    let ox = dx < 0 ? -1 : (dx >= Tile.GRID_SIZE ? 1 : 0)
                    let oy = dy < 0 ? -1 : (dy >= Tile.GRID_SIZE ? 1 : 0)
                    const ndata = Client.instance.map[(this.x + ox) + "_" + (this.y + oy)];
                    // можем выйти за границы карты и такого грида не будет
                    if (ndata !== undefined) {
                        let ix = dx < 0 ? Tile.GRID_SIZE + dx : (dx >= Tile.GRID_SIZE ? dx - Tile.GRID_SIZE : dx)
                        let iy = dy < 0 ? Tile.GRID_SIZE + dy : (dy >= Tile.GRID_SIZE ? dy - Tile.GRID_SIZE : dy)
                        tn = ndata[iy * Tile.GRID_SIZE + ix]
                    }
                }
                tr[rx + 1][ry + 1] = tn
            }
        }

        if (tr[0][0] >= tr[1][0]) tr[0][0] = -1
        if (tr[0][0] >= tr[0][1]) tr[0][0] = -1
        if (tr[2][0] >= tr[1][0]) tr[2][0] = -1
        if (tr[2][0] >= tr[2][1]) tr[2][0] = -1
        if (tr[0][2] >= tr[0][1]) tr[0][2] = -1
        if (tr[0][2] >= tr[1][2]) tr[0][2] = -1
        if (tr[2][2] >= tr[2][1]) tr[2][2] = -1
        if (tr[2][2] >= tr[1][2]) tr[2][2] = -1

        for (let i = data[idx]; i >= 0; i--) {
            const ts = Tile.sets[i]
            if (ts == undefined || ts.corners == undefined || ts.borders == undefined) continue
            let bm = 0
            let cm = 0
            for (let o = 0; o < 4; o++) {
                if (tr[Grid.bx[o]][Grid.by[o]] == i) bm |= 1 << o
                if (tr[Grid.cx[o]][Grid.cy[o]] == i) cm |= 1 << o
            }
            if (bm !== 0) {
                const arr = ts.borders[bm - 1];
                if (arr !== undefined) {
                    let path = arr.get(getRandomByCoord(x, y))
                    if (path !== undefined) {
                        if (path.includes(".")) path = "assets/" + path
                        let spr = PIXI.Sprite.from(path)
                        spr.x = sx
                        spr.y = sy
                        container.addChild(spr)
                    }
                }
            }
            if (cm !== 0) {
                const arr = ts.corners[cm - 1];
                if (cm > 1) console.log("cm ", cm, ts.corners)
                if (arr !== undefined) {
                    let path = arr.get(getRandomByCoord(x, y))
                    if (path !== undefined) {
                        if (path.includes(".")) path = "assets/" + path
                        let spr = PIXI.Sprite.from(path)
                        spr.x = sx
                        spr.y = sy
                        container.addChild(spr)
                    }
                }
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
