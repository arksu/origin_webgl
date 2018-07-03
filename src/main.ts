import * as PIXI from 'pixi.js';

class Core {
    private gameWidth: number;
    private gameHeight: number;

    private app: PIXI.Application;

    constructor() {
        this.gameWidth = window.innerWidth;
        this.gameHeight = window.innerHeight;

        let options: PIXI.ApplicationOptions = {};
        options.view = <HTMLCanvasElement>document.getElementById("game");
        options.backgroundColor = 0x222222;

        // init PIXI
        this.app = new PIXI.Application(this.gameWidth, this.gameHeight, options);
        console.log("game size: " + this.gameWidth + "x" + this.gameHeight);

        // disable loading label
        document.getElementById("loadingLabel").style.display = "none";
    }

    public start() {
        let texture = PIXI.Texture.fromImage('assets/logo.png');
        let logo = new PIXI.Sprite(texture);

        this.app.stage.addChild(logo);

        logo.anchor.x = 0.5;
        logo.anchor.y = 0.5;
        logo.position.x = this.gameWidth / 2;
        logo.position.y = this.gameHeight / 3;
    }

    // TODO: delete?
    public sizeHandler() {
        let w = this.gameWidth;
        let h = this.gameHeight;

        let maxWidth = window.innerWidth;
        let maxHeight = window.innerHeight;

        let ratio = maxWidth / w;
        if (h * ratio > maxHeight) {
            ratio = maxHeight / h;
        }

        let aw = w * ratio;
        let ah = h * ratio;
        let dx = maxWidth - aw;
        let dy = maxHeight - ah;

        if (dx > 0) {
            this.app.renderer.view.style.left = dx / 2 + "px";
        } else {
            this.app.renderer.view.style.left = "0";
        }
        if (dy > 0) {
            this.app.renderer.view.style.top = dy / 2 + "px";
        } else {
            this.app.renderer.view.style.top = "0";
        }
        this.app.renderer.view.style.width = aw + "px";
        this.app.renderer.view.style.height = ah + "px";
    };

    public changeResolution(w: number, h: number) {
        if (this.gameWidth !== w || this.gameHeight !== h) {
            console.log("changeResolution: " + w + "x" + h);
            this.gameWidth = w;
            this.gameHeight = h;
            this.app.renderer.resize(w, h);
            // this.sizeHandler();
        }
    };
}

let core = new Core();

window.onresize = function () {
    core.changeResolution(window.innerWidth, window.innerHeight);
};
document.addEventListener('DOMContentLoaded', function () {
    core.changeResolution(window.innerWidth, window.innerHeight);
});

core.start();
