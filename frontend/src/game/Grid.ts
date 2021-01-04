import {Application, Container, Sprite, Texture} from "pixi.js";
import Tile from "@/game/Tile";
import Client from "@/net/Client";

export default class Grid {

    public container: Container;

    private app: Application

    private x: number
    private y: number

    constructor(app: Application, x: number, y: number) {
        this.container = new Container();
        this.app = app;
        this.x = x;
        this.y = y;

        let mul = 0.5;

        this.container.x = 1000 + x * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE * mul - y * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE * mul;
        this.container.y = -1600 + x * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE * mul + y * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE * mul;

        console.log(this.container.x)
        console.log(this.container.y)

        this.container.scale.x = mul;
        this.container.scale.y = mul;


        this.makeTiles();
    }

    public makeTiles() {
        let key = this.x + "_" + this.y;
        let data = Client.instance.map[key];

        for (let x = 0; x < Tile.GRID_SIZE; x++) {
            for (let y = 0; y < Tile.GRID_SIZE; y++) {

                let tn = Tile.getTextureName(data[y * Tile.GRID_SIZE + x])

                let t = Texture.from(tn);

                let s = Sprite.from(t);
                s.roundPixels = true;

                this.container.addChild(s)

                s.x = x * Tile.TILE_WIDTH_HALF - y * Tile.TILE_WIDTH_HALF;
                s.y = x * Tile.TILE_HEIGHT_HALF + y * Tile.TILE_HEIGHT_HALF;
            }
        }
    }
}