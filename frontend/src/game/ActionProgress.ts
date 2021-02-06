import {ActionProgressData} from "@/net/Packets";
import * as PIXI from 'pixi.js';

export default class ActionProgress {
    public sprite: PIXI.Sprite

    constructor(ap: ActionProgressData) {
        let cn = (ap.c / ap.t) * 21
        console.log("action progres ", cn)
        let tn = "clock" + Math.round(cn)
        console.log("texture ", tn)
        this.sprite = PIXI.Sprite.from(tn)
    }

    public apply(ap: ActionProgressData) {
        let cn = (ap.c / ap.t) * 21
        console.log("action progres ", cn)
        let tn = "clock" + Math.round(cn)
        console.log("texture ", tn)
        
        this.sprite.texture = PIXI.Texture.from(tn)
    }

    public destroy() {
        this.sprite.destroy({
            children: true
        })
    }
}