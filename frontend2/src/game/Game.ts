import * as PIXI from 'pixi.js';

export default class Game {
    private static readonly canvas: HTMLCanvasElement = <HTMLCanvasElement>window.document.getElementById("game");

    public static instance?: Game = undefined;

    /**
     * PIXI app
     */
    readonly app: PIXI.Application;

    public static start() {
        console.warn("pixi start");

        this.canvas.style.display = "block";
        this.instance = new Game();
    }

    public static stop() {
        console.warn("pixi stop");

        Game.instance?.destroy();
        Game.instance = undefined;
        // Client.instance.clear();

        this.canvas.style.display = "none";
    }


    private constructor() {
        // сглаживание пикселей при масштабировании
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

        this.app = new PIXI.Application({
            width: window.innerWidth,
            height: window.innerHeight,
            view: Game.canvas,
            autoDensity: true,
            preserveDrawingBuffer: true,
            powerPreference: 'high-performance',
            // resizeTo: window,
            antialias: false,
            // clearBeforeRender: true,
            backgroundColor: 0x333333
        });
    }

    private destroy() {
        this.app.destroy(false, {
            children: true,
        });
    }
}