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
            case 0 :
                return 'grass1'
            case 1 :
                return 'stone1'
            default :
                // let r = getRandomInt(3) + 1
                // return 'test_tile_' + r
                let r = getRandomInt(19) + 1
                return 'forest_grass_' + r
        }
    }
}