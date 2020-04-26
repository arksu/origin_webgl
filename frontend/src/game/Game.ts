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

        let gameWidth = window.innerWidth;
        let gameHeight = window.innerHeight;
        console.log("game: " + gameWidth + " x " + gameWidth);

        let opt: PIXI.ApplicationOptions = {
            width: gameWidth,
            height: gameHeight,
            backgroundColor: 0x000000,
            view: canvas
        };

        const app = new PIXI.Application(opt);
        app.renderer.autoResize = true;

        let rectangle = new PIXI.Graphics();
        rectangle.beginFill(0x66CCFF);
        rectangle.lineStyle(4, 0xFF3300, 1);
        rectangle.drawRect(10, 20, 200, 100);
        rectangle.endFill();

        app.stage.addChild(rectangle);

        window.onresize = function () {
            app.renderer.resize(window.innerWidth, window.innerHeight);
        };
    }
}