import {getRandomInt} from "@/utils/Util";
import water from "./tiles/water.json"
import stone from "./tiles/stone.json"
import forest_leaf from "./tiles/forest_leaf.json"
import forest_pine from "./tiles/forest_pine.json"

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

    public static init() {
        Tile.sets[3] = new TileSet(water)
        Tile.sets[10] = new TileSet(forest_leaf)
        Tile.sets[15] = new TileSet(forest_pine)
        Tile.sets[18] = new TileSet(stone)
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