import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";
import Client from "@/net/Client";

export default class Grid {

    public container: PIXI.Container;

    private app: PIXI.Application

    private x: number
    private y: number

    private tiles: PIXI.Sprite[] = [];

    constructor(app: PIXI.Application, x: number, y: number) {
        this.container = new PIXI.Container();
        this.app = app;
        this.x = x;
        this.y = y;

        let mul = 1;

        // координаты грида ставим в абсолютные мировые в тайлах
        this.container.x = x * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - y * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - Tile.TILE_WIDTH_HALF;
        this.container.y = x * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE + y * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE;

        // this.container.scale.x = mul;
        // this.container.scale.y = mul;

        console.log("grid screen x=" + this.container.x + " y=" + this.container.y)

        this.makeTiles();

        // агрессивное кэширование гридов карты, иначе каждый раз все рендерится потайлово
        this.container.cacheAsBitmap = true
    }

    public destroy() {
        this.container.destroy()
    }

    public makeTiles() {
        let key = this.x + "_" + this.y;
        let data = Client.instance.map[key];

        for (let x = 0; x < Tile.GRID_SIZE; x++) {
            for (let y = 0; y < Tile.GRID_SIZE; y++) {

                let tn = Tile.getTextureName(data[y * Tile.GRID_SIZE + x])

                let s = PIXI.Sprite.from(tn);
                s.roundPixels = false;
                // s.tint = 50000 * (this.x % 2 + this.y % 2);

                this.container.addChild(s)

                s.x = x * Tile.TILE_WIDTH_HALF - y * Tile.TILE_WIDTH_HALF;
                s.y = x * Tile.TILE_HEIGHT_HALF + y * Tile.TILE_HEIGHT_HALF;

                this.tiles[y * Tile.GRID_SIZE + x] = s;
            }
        }
    }
}