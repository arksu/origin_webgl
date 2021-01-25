import {getRandomInt} from "@/utils/Util";

export default class Tile {

    /**
     * размеры текстуры тайла
     */
    public static readonly TEXTURE_WIDTH = 64;
    public static readonly TEXTURE_HEIGHT = 32;

    public static readonly TILE_WIDTH_HALF = Tile.TEXTURE_WIDTH / 2;
    public static readonly TILE_HEIGHT_HALF = Tile.TEXTURE_HEIGHT / 2;

    public static readonly GRID_SIZE = 100;

    public static readonly TILE_SIZE = 12;

    public static getTextureName(t: number, x: number, y: number): string {
        switch (t) {
            // case 0 :
            //     return this.randomNames('tiles/grass', 2)
            case 1 :
                return this.randomNames('tiles/grass', 2)
            case 2:
                return this.randomNames('tiles/water', 1)
            case 3:
                return this.randomNames('tiles/stone', 3)
            case 4 :
                return this.randomNames('tiles/forest_grass_', 1)
            default :
                return 'unknown'
        }
    }

    private static randomNames(s: string, n: number): string {
        let r = getRandomInt(n) + 1
        return s + r + '.png'
    }
}