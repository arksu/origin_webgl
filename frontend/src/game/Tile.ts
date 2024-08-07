import * as PIXI from 'pixi.js';
import {getRandomByCoord} from "@/util/random";

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
import terrainHeath from "./terrain/heath.json"
import type Coord from '@/util/Coord'

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
        // eslint-disable-next-line no-constant-condition
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
    p: number
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

    constructor(d: TerrainObjectData) {
        this.data = d
        this.sz = this.data.layers.length
    }

    /**
     * сгенерировать список текстур объекта
     * @param x координаты тайла
     * @param y координаты тайла
     * @param sx экранные координаты центра тайла куда надо поместить объект
     * @param sy экранные координаты центра тайла куда надо поместить объект
     */
    public generate(x: number, y: number, sx: number, sy: number): PIXI.Sprite[] | undefined {
        if (this.sz > 0) {
            const list: PIXI.Sprite[] = []

            let isShadow = false;
            let seed = getRandomByCoord(x, y)
            let cnt = 0
            do {
                for (let i = 0; i < this.sz; i++) {
                    const l = this.data.layers[i]
                    if (l.p == 0) {
                        isShadow = true
                    }
                    // проверим шанс генерации
                    seed = getRandomByCoord(x, y, i, seed);
                    if ((l.p == 0 && list.length == 0) || (seed % l.p) == 0) {
                        const path = l.img
                        const spr = PIXI.Sprite.from(path)
                        const dx = -this.data.offset[0] + l.offset[0]
                        const dy = -this.data.offset[1] + l.offset[1]

                        spr.x = sx + dx
                        spr.y = sy + dy
                        spr.zIndex = 100
                        const z = l.z
                        if (z !== undefined) {
                            spr.zIndex += z
                        }
                        list.push(spr)
                    }
                }
                cnt++
            } while (isShadow && list.length < 2 && this.sz > 1 && cnt < 10)

            return list

        } else {
            return undefined
        }
    }
}

class TerrainObjects {
    list: TerrainObject[] = []

    constructor(d: any) {
        for (let i = 0; i < d.length; i++) {
            const to = new TerrainObject(d[i])
            this.list.push(to)
        }
    }

    /**
     * сгенерировать список текстур террейн объекта (составляющих 1 террейн объект)
     * в списке спрайты уже будут иметь нужные координаты
     * @param x координаты тайла
     * @param y координаты тайла
     * @param sx экранные координаты центра тайла куда надо поместить объект
     * @param sy экранные координаты центра тайла куда надо поместить объект
     */
    public generate(x: number, y: number, sx: number, sy: number): PIXI.Sprite[] | undefined {
        // идем по всему списку
        for (let i = 0; i < this.list.length; i++) {
            // у каждого элемента проверяем шанс на спавн
            if ((getRandomByCoord(x, y)) % this.list[i].data.chance == 0) {
                // генерируем террейн объект
                return this.list[i].generate(x, y, sx, sy)
            }
        }
        return undefined
    }
}

export default class Tile {
    public static readonly ATLAS = 'assets/game/tiles.png'

    public static sets: TileSet[] = []
    public static terrains: TerrainObjects[] = []

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
        Tile.terrains[13] = new TerrainObjects(terrainWald)

        Tile.sets[15] = new TileSet(leaf)

        // Todo пустошь
        Tile.sets[16] = new TileSet(fen)

        Tile.sets[17] = new TileSet(grass)

        Tile.sets[18] = new TileSet(heath2)
        Tile.terrains[18] = new TerrainObjects(terrainHeath)
        Tile.sets[21] = new TileSet(moor)
        Tile.sets[23] = new TileSet(swamp)
        Tile.sets[29] = new TileSet(clay)
        Tile.sets[30] = new TileSet(dirt)

        Tile.sets[32] = new TileSet(sand)
    }

    public static getGroundTexture(t: number, x: number, y: number): string | undefined {
        const set = Tile.sets[t]
        if (set !== undefined) {
            return set.ground.get(getRandomByCoord(x, y))
        }
        return undefined
    }
}

Tile.init()

export function coordGame2Screen(x: number, y: number): Coord {
    const px = x / Tile.TILE_SIZE;
    const py = y / Tile.TILE_SIZE;
    return [px * Tile.TILE_WIDTH_HALF - py * Tile.TILE_WIDTH_HALF, px * Tile.TILE_HEIGHT_HALF + py * Tile.TILE_HEIGHT_HALF];
}