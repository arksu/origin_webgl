export default class Tile {

    /**
     * размеры текстуры тайла
     */
    public static readonly TEXTURE_WIDTH = 64;
    public static readonly TEXTURE_HEIGTH = 32;

    public static readonly TILE_WIDTH_HALF = Tile.TEXTURE_WIDTH / 2;
    public static readonly TILE_HEIGHT_HALF = Tile.TEXTURE_HEIGTH / 2;

    public static readonly GRID_SIZE = 100;

    public static getTextureName(t: number): string {
        switch (t) {
            case 0 :
                return 'grass1.png'
            case 1 :
                return 'stone1.png'
            default :
                return 'swamp1.png'
        }
    }
}