import * as PIXI from 'pixi.js';
import {useGameStore} from "../store/game";

export default class Render {

    public static instance?: Render = undefined;

    /**
     * PIXI app
     */
    private readonly app: PIXI.Application;

    /**
     * хранилище игровых данных
     * @private
     */
    private readonly store = useGameStore()

    public static start() {
        console.warn("pixi start");
        this.instance = new Render();
    }

    public static stop() {
        console.warn("pixi stop");

        Render.instance?.destroy();
        Render.instance = undefined;
    }


    private constructor() {
        // сглаживание пикселей при масштабировании
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

        this.app = new PIXI.Application({
            width: window.innerWidth,
            height: window.innerHeight,
            view: <HTMLCanvasElement>window.document.getElementById("game"),
            autoDensity: true,
            preserveDrawingBuffer: true,
            powerPreference: 'high-performance',
            antialias: false,
            backgroundColor: 0x333333
        });
    }

    private destroy() {
        this.app.destroy(false, {
            children: true,
        });
    }
}
