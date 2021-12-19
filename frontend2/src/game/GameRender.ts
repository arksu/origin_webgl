import * as PIXI from 'pixi.js';

export default class GameRender {

    public static instance?: GameRender = undefined;

    /**
     * PIXI app
     */
    readonly app: PIXI.Application;

    public static start() {
        console.warn("pixi start");
        this.instance = new GameRender();
    }

    public static stop() {
        console.warn("pixi stop");

        GameRender.instance?.destroy();
        GameRender.instance = undefined;
        // Client.instance.clear();
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