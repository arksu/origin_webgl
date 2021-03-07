import * as PIXI from 'pixi.js';

import {Coord, getRandomByCoord, getRandomInt} from "@/utils/Util";
import water from "./tiles/water.json"
import water_deep from "./tiles/water_deep.json"
import swamp from "./tiles/swamp.json"
import wald from "./tiles/wald.json"
import heath2 from "./tiles/heath2.json"

import leaf from "./tiles/leaf.json"
import plowed from "./tiles/plowed.json"
import dirt from "./tiles/dirt.json"
import floor_stone from "./tiles/floor_stone.json"
import sand from "./tiles/sand.json"
import clay from "./tiles/clay.json"
import grass from "./tiles/grass.json"
import moor from "./tiles/moor.json"
import fen from "./tiles/fen.json"

import terrainWald from "./terrain/wald.json"

interface ResTile {
    img: string
    w: number
}

class TileArray {
    tiles: ResTile[]

    /**
     * total weight
     */
    tw: number

    /**
     * size
     */
    sz: number

    constructor(list: ResTile[]) {
        this.tiles = list
        this.sz = list.length
        this.tw = 0
        for (let i = 0; i < list.length; i++) {
            this.tw += list[i].w
        }
    }

    public get(t: number): string | undefined {
        if (this.tw == 0) return undefined
        let w = t % this.tw
        let i = 0
        while (true) {
            if ((w -= this.tiles[i].w) < 0) {
                break
            }
            i++
        }
        return this.tiles[i].img
    }
}

class TileSet {
    ground: TileArray
    borders: TileArray[] = []
    corners: TileArray[] = []

    constructor(d: any) {
        this.ground = new TileArray(d.ground)
        for (let i = 0; i < d.borders.length; i++) {
            this.borders.push(new TileArray(d.borders[i]))
        }
        for (let i = 0; i < d.corners.length; i++) {
            this.corners.push(new TileArray(d.corners[i]))
        }
    }
}

interface TerrainLayer {
    img: string
    offset: Coord
    w: number
    z?: number
}

interface TerrainObjectData {
    chance: number
    offset: Coord
    layers: TerrainLayer[]
}

class TerrainObject {
    data: TerrainObjectData
    sz: number
    tw: number

    constructor(d: TerrainObjectData) {
        this.data = d
        this.sz = this.data.layers.length
        this.tw = 0
        for (let i = 0; i < this.data.layers.length; i++) {
            this.tw += this.data.layers[i].w
        }
    }

    public generate(x: number, y: number, sx: number, sy: number): PIXI.Sprite | undefined {
        // TODO детерменированный рандом в зависимости от координат и шанса генерации
        let idx = this.get(getRandomByCoord(x, y))
        if (idx >= 0) {
            let path = this.data.layers[idx].img
            if (path.includes(".")) path = "assets/" + path
            let spr = PIXI.Sprite.from(path)
            let dx = -this.data.offset[0] + this.data.layers[idx].offset[0]
            let dy = -this.data.offset[1] + this.data.layers[idx].offset[1]

            spr.x = sx + dx
            spr.y = sy + dy
            spr.zIndex = 100
            let z = this.data.layers[idx].z
            if (z !== undefined) {
                spr.zIndex += z
            }
            return spr
        }
    }

    public get(t: number): number {
        if (this.tw == 0) return -1
        let w = t % this.tw
        let i = 0
        while (true) {
            if ((w -= this.data.layers[i].w) < 0) {
                break
            }
            i++
        }
        return i
    }
}

class TileObjects {
    list: TerrainObject[] = []

    constructor(d: any) {
        for (let i = 0; i < d.length; i++) {
            let to = new TerrainObject(d[i])
            this.list.push(to)
        }
    }

    public generate(x: number, y: number, sx: number, sy: number): PIXI.Sprite | undefined {
        // TODO детерменированный рандом в зависимости от координат и шанса генерации
        for (let i = 0; i < this.list.length; i++) {
            if (getRandomInt(this.list[i].data.chance) == 0) {
                return this.list[i].generate(x, y, sx, sy)
            }
        }
        return undefined
    }
}


export default class Tile {
    public static sets: TileSet[] = []
    public static terrains: TileObjects[] = []

    /**
     * размеры текстуры тайла
     */
    public static readonly TEXTURE_WIDTH = 64;
    public static readonly TEXTURE_HEIGHT = 32;

    public static readonly TILE_WIDTH_HALF = Tile.TEXTURE_WIDTH / 2;
    public static readonly TILE_HEIGHT_HALF = Tile.TEXTURE_HEIGHT / 2;

    public static readonly GRID_SIZE = 100;

    public static readonly TILE_SIZE = 12;

    public static readonly FULL_GRID_SIZE = Tile.GRID_SIZE * Tile.TILE_SIZE

    public static init() {
        Tile.sets[1] = new TileSet(water_deep)
        Tile.sets[3] = new TileSet(water)

        Tile.sets[10] = new TileSet(floor_stone)
        Tile.sets[11] = new TileSet(plowed)


        Tile.sets[13] = new TileSet(wald)
        Tile.terrains[13] = new TileObjects(terrainWald)

        Tile.sets[15] = new TileSet(leaf)

        // Todo пустошь
        Tile.sets[16] = new TileSet(fen)

        Tile.sets[17] = new TileSet(grass)

        Tile.sets[18] = new TileSet(heath2)
        Tile.sets[21] = new TileSet(moor)
        Tile.sets[23] = new TileSet(swamp)
        Tile.sets[29] = new TileSet(clay)
        Tile.sets[30] = new TileSet(dirt)

        Tile.sets[32] = new TileSet(sand)
    }

    public static getGroundTexture(t: number): string | undefined {
        const set = Tile.sets[t]
        if (set !== undefined) {
            return set.ground.get(getRandomInt(25000))
        }
        return undefined
    }

    private static randomNames(s: string, n: number): string {
        let r = getRandomInt(n) + 1
        return s + r + '.png'
    }
}