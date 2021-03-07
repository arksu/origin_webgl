import {getRandomInt} from "@/utils/Util";
import water from "./tiles/water.json"
import water_deep from "./tiles/water_deep.json"
import swamp from "./tiles/swamp.json"
import wald from "./tiles/wald.json"
import wald2 from "./tiles/wald2.json"
import swamp2 from "./tiles/swamp2.json"
import grass2 from "./tiles/grass2.json"
import heath2 from "./tiles/heath2.json"
import bog2 from "./tiles/bog2.json"
import floor_stone2 from "./tiles/floor_stone2.json"
import floor_mine2 from "./tiles/floor_mine2.json"
import floor_wood2 from "./tiles/floor_wood2.json"
import water2 from "./tiles/water2.json"
import water_deep2 from "./tiles/water_deep2.json"

import leaf from "./tiles/leaf.json"
import plowed from "./tiles/plowed.json"
import dirt from "./tiles/dirt.json"
import floor_stone from "./tiles/floor_stone.json"
import sand from "./tiles/sand.json"
import clay from "./tiles/clay.json"
import grass from "./tiles/grass.json"
import mountain from "./tiles/mountain.json"
import moor from "./tiles/moor.json"
import heath from "./tiles/heath.json"
import fen from "./tiles/fen.json"
import bog from "./tiles/bog.json"

import forest_pine from "./tiles/forest_pine.json"
import stone from "./tiles/stone.json"

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


export default class Tile {
    public static sets: TileSet[] = []

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
        // Tile.sets[11] = new TileSet(dirt)
        Tile.sets[10] = new TileSet(leaf)
        // Tile.sets[150] = new TileSet(clay)
        // Tile.sets[153] = new TileSet(wald2) // Daji's wald
        // Tile.sets[15] = new TileSet(swamp2)  // Daji's swamp
        // Tile.sets[15] = new TileSet(swamp)
        // Tile.sets[15] = new TileSet(heath2)
        Tile.sets[15] = new TileSet(bog)
        // Tile.sets[15] = new TileSet(grass)
        // Tile.sets[154] = new TileSet(wald)
        // Tile.sets[16] = new TileSet(forest_pine) // TODO
        // Tile.sets[18] = new TileSet(stone)
        // Tile.sets[20] = new TileSet(clay)

        // Tile.sets[221] = new TileSet(sand)
        Tile.sets[22] = new TileSet(plowed)
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