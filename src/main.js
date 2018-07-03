import * as PIXI from 'pixi.js';


function init() {
    window.gameWidth = window.innerWidth;
    window.gameHeight = window.innerHeight;

    // init PIXI
    const app = new PIXI.Application(gameWidth, gameHeight, {
        view: document.getElementById("game"),
        backgroundColor: "0x222222"
    });
    console.log("game size: " + gameWidth + "x" + gameHeight);
    const renderer = app.renderer;
    console.log(renderer);

    // disable loading label
    document.getElementById("loadingLabel").style.display = "none";

    // set up base window functions
    window.changeResolution = function (w, h) {
        if (window.gameWidth !== w || window.gameHeight !== h) {
            console.log("changeResolution: " + w + "x" + h);
            window.gameWidth = w;
            window.gameHeight = h;
            renderer.resize(w, h);
            sizeHandler();
        }
    };

    window.sizeHandler = function () {
        let w = window.gameWidth;
        let h = window.gameHeight;

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
            renderer.view.style.left = dx / 2 + "px";
        } else {
            renderer.view.style.left = 0;
        }
        if (dy > 0) {
            renderer.view.style.top = dy / 2 + "px";
        } else {
            renderer.view.style.top = 0;
        }
        renderer.view.style.width = aw + "px";
        renderer.view.style.height = ah + "px";
    };
    window.onresize = function () {
        changeResolution(window.innerWidth, window.innerHeight);
    };
    document.onready = function () {
        changeResolution(window.innerWidth, window.innerHeight);
    };
    return app;
}

function start(app) {
    let texture = PIXI.Texture.fromImage('assets/logo.png');
    let logo = new PIXI.Sprite(texture);

    app.stage.addChild(logo);

    logo.anchor.x = 0.5;
    logo.anchor.y = 0.5;
    logo.position.x = gameWidth / 2;
    logo.position.y = gameHeight / 3;
}

start(init());
sizeHandler();
