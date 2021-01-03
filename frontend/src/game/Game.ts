import {Application} from "pixi.js"

/**
 * основная игровая логика (графика и тд)
 */
export default class Game {

    public static start() {
        let canvas = <HTMLCanvasElement>document.getElementById("game");
        let appdiv = <HTMLElement>document.getElementById("app");
        canvas.style.display = "block";
        appdiv.style.display = "none";

        this.startPixi(canvas);
    }

    public static stop() {
        let canvas = <HTMLCanvasElement>document.getElementById("game");
        let appdiv = <HTMLElement>document.getElementById("app");
        canvas.style.display = "none";
        appdiv.style.display = "block";
    }

    private static startPixi(canvas: HTMLCanvasElement) {
        let app = new Application({view: canvas, width: 200, height: 400, autoDensity: false})
        app.renderer.backgroundColor = 0x061639;
        app.renderer.resize(window.innerWidth, window.innerHeight);
    }
}