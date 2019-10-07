import * as _ from "lodash";
import * as PIXI from "pixi.js";
import ApplicationOptions = PIXI.ApplicationOptions;

window._ = _;

let opt : ApplicationOptions =  {
    width: 400,
    height : 300,
    view:  <HTMLCanvasElement>document.getElementById("game")
};

const app = new PIXI.Application(opt);

let rectangle = new PIXI.Graphics();
rectangle.beginFill(0x66CCFF);
rectangle.lineStyle(4, 0xFF3300, 1);
rectangle.drawRect(10,20, 200,100);
rectangle.endFill();

app.stage.addChild(rectangle);

console.log("yay!");