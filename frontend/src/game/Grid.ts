import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import Client from "@/net/Client";
import {getRandomByCoord} from "@/utils/Util";

export interface MapData {
    x: number
    y: number

    /**
     * массив тайлов
     */
    tiles: number[]

    /**
     * было ли изменение тайлов
     */
    isChanged: boolean
}

export default class Grid {
    // 2 4 5
    private static readonly DIVIDER = 4

    /**
     * размер одной стороны чанка
     */
    private readonly CHUNK_SIZE = Tile.GRID_SIZE / Grid.DIVIDER

    private static readonly bx = [0, 1, 2, 1]
    private static readonly by = [1, 0, 1, 2]
    private static readonly cx = [0, 0, 2, 2]
    private static readonly cy = [0, 2, 2, 0]

    /**
     * чанки-контейнеры на которые делим грид (иначе по размеру текстуры целый грид не влезает в память)
     */
    public containers: PIXI.Container[] = [];

    private parent: PIXI.Container

    /**
     * координаты грида (в координатах гридов)
     */
    public readonly x: number
    public readonly y: number
    /**
     * абсолютные игровые координаты
     */
    public readonly absoluteX: number
    public readonly absoluteY: number

    public readonly key: string

    private spriteTextureNames: string[] = []
    private sprites: PIXI.Sprite[] = [];

    private _visible: boolean = true

    constructor(parent: PIXI.Container, x: number, y: number) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.absoluteX = x * Tile.FULL_GRID_SIZE + Tile.FULL_GRID_SIZE / 2
        this.absoluteY = y * Tile.FULL_GRID_SIZE + Tile.FULL_GRID_SIZE / 2
        this.key = this.x + "_" + this.y
        // console.log("new grid", this.key)

// замерим время на создание грида
        const timerName = "make grid " + this.key;
        console.time(timerName)
        this.makeChunks()
        console.timeEnd(timerName)

        // агрессивное кэширование гридов карты, иначе каждый раз все рендерится потайлово
        for (let i = 0; i < this.containers.length; i++) {
            const c = this.containers[i];
            setTimeout(() => {
                c.cacheAsBitmap = true
            }, i * 5 + 40)
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
                this.parent.addChild(container)
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

    /**
     * управление видимостью грида (скрываем для кэширования)
     */
    public set visible(v: boolean) {
        if (this._visible != v) {
            this._visible = v
            for (let i = 0; i < this.containers.length; i++) {
                this.containers[i].visible = v
            }
        }
    }

    public get visible() {
        return this._visible
    }

    /**
     * перестроить грид (данные тайлов изменились)
     */
    public rebuild() {
        console.log("grid rebuild", this.key)
        // TODO перестраивать только ту часть грида которая реально изменилась
        //  надо делать дифф тайлов по которым построли чанки и актуальными тайлами

        this.destroy()
        this.makeChunks()
        // агрессивное кэширование гридов карты, иначе каждый раз все рендерится потайлово
        for (let i = 0; i < this.containers.length; i++) {
            const c = this.containers[i];
            setTimeout(() => {
                c.cacheAsBitmap = true
            }, i * 30)
        }
    }

    private makeChunk(cx: number, cy: number, idx: number): PIXI.Container {
        let container = new PIXI.Container();
        container.sortableChildren = true
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
        const data = Client.instance.map[this.key].tiles;

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
                    // if (path.includes(".")) path = "assets/" + path

                    let spr = PIXI.Sprite.from(path);

                    // spr.tint = 500000 * chunk;

                    const sx = tx * Tile.TILE_WIDTH_HALF - ty * Tile.TILE_WIDTH_HALF
                    const sy = tx * Tile.TILE_HEIGHT_HALF + ty * Tile.TILE_HEIGHT_HALF
                    spr.x = sx;
                    spr.y = sy;
                    container.addChild(spr)

                    this.sprites[idx] = spr;

                    this.makeTransparentTiles(container, data, idx, x, y, sx, sy)
                    this.makeTerrainObjects(container, data[idx], x, y,
                        // sx, sy)
                        sx + Tile.TILE_WIDTH_HALF, sy + Tile.TILE_HEIGHT_HALF)
                }
            }
        }
    }

    private makeTransparentTiles(container: PIXI.Container, data: number[], idx: number, x: number, y: number, sx: number, sy: number) {
        const tr: number[][] = []
        // идем по тайлам вокруг целевого и заполним массив окружающих тайлов tr
        for (let rx = -1; rx <= 1; rx++) {
            tr[rx + 1] = []
            for (let ry = -1; ry <= 1; ry++) {
                if (rx == 0 && ry == 0) {
                    tr[rx + 1][ry + 1] = 0
                    continue
                }

                const dx = x + rx
                const dy = y + ry
                let tn = -1
                // это тайл еще текущего грида
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
                        tn = ndata.tiles[iy * Tile.GRID_SIZE + ix]
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

        // текущий (центральный тайл)
        for (let i = data[idx] - 1; i >= 0; i--) {
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
                        let tn = path
                        // if (path.includes(".")) path = "assets/" + path
                        let spr = PIXI.Sprite.from(path)
                        spr.x = sx
                        spr.y = sy
                        let idx = this.spriteTextureNames.length
                        this.spriteTextureNames[idx] = tn
                        this.sprites[idx] = spr
                        container.addChild(spr)
                    }
                }
            }
            if (cm !== 0) {
                const arr = ts.corners[cm - 1];
                if (arr !== undefined) {
                    let path = arr.get(getRandomByCoord(x, y))
                    if (path !== undefined) {
                        let tn = path
                        // if (path.includes(".")) path = "assets/" + path
                        let spr = PIXI.Sprite.from(path)
                        spr.x = sx
                        spr.y = sy
                        let idx = this.spriteTextureNames.length
                        this.spriteTextureNames[idx] = tn
                        this.sprites[idx] = spr
                        container.addChild(spr)
                    }
                }
            }
        }
    }

    private makeTerrainObjects(container: PIXI.Container, t: number, x: number, y: number, sx: number, sy: number) {
        let terrain = Tile.terrains[t]
        if (terrain !== undefined) {
            let sprList = terrain.generate(x, y, sx, sy)
            if (sprList !== undefined) {
                for (let i = 0; i < sprList.length; i++) {
                    container.addChild(sprList[i])
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

            for (let i = 0; i < this.sprites.length; i++) {
                let spr = this.sprites[i]

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
