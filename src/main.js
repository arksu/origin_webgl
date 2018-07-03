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
    console.profile('111');
    let texture = PIXI.Texture.fromImage('assets/logo.png');
    let logo = new PIXI.Sprite(texture);

    let renderTexture = PIXI.RenderTexture.create(500, 400);
    let blur_filter = new PIXI.filters.BlurFilter();
    blur_filter.blurY = 14;
    logo.filters = [blur_filter];

    // app.stage.addChild(logo);

    app.renderer.render(logo, renderTexture);
    app.renderer.flush();
    // renderTexture.render(logo, null, true);
    // logo.render(renderTexture);

    // logo.filters = undefined;

    let logo2 = new PIXI.Sprite(renderTexture);
    app.stage.addChild(logo2);

    console.log(renderTexture);
    console.log(renderTexture.baseTexture.valid);
    console.log(logo2);

    logo2.position.x = 10;
    logo2.position.y = 10;


    logo.anchor = {x: 0.5, y: 0.5};
    logo.position.x = gameWidth / 2;
    logo.position.y = gameHeight / 3;

    console.profileEnd('111');

    let cnt = 1;
    let f = true;
    console.log(app);
    app.ticker.add(function () {
        cnt--;
        if (cnt > 0) {
            let r = app.renderer.render(logo, renderTexture);
            app.renderer.flush();
        } else {
            if (f) {
                f = false;
                console.log(renderTexture);
                console.log(renderTexture.baseTexture.valid);
            }
        }
    });

}

start(init());
sizeHandler();
