import * as PIXI from "pixi.js";

export default class {

    public static start() {
        document.getElementById("game").style.display = "block";
        this.startPixi();
    }

    public static stop() {
        document.getElementById("game").style.display = "none";

    }

    private static startPixi() {
        let canvas = <HTMLCanvasElement>document.getElementById("game");

    }
}