import {GameObject} from "@/game/GameObject";
import Game from "@/game/Game";

export default class MoveController {

    private readonly me: GameObject

    toX!: number
    toY!: number
    currentX!: number
    currentY!: number
    speed!: number
    moveType!: string

    constructor(obj: GameObject, data: any) {
        this.me = obj
        this.applyData(data)

        if (Game.instance !== undefined) {
            Game.instance.movingObjects[obj.id] = obj
        }
    }

    public applyData(data: any) {
        this.currentX = data.x
        this.currentY = data.y
        this.toX = data.tx
        this.toY = data.ty
        this.speed = data.s
        this.moveType = data.mt
    }

    public stop() {
        if (Game.instance !== undefined) {
            delete Game.instance.movingObjects[this.me.id]
        }
    }

    public update(dt: number) {
        // console.log(this)
        // дистанция до конечной точки по данным сервера
        let d = Math.sqrt(Math.pow(this.toX - this.currentX, 2) + Math.pow(this.toY - this.currentY, 2))

        // console.log("d=", d)

        // сколько прошли
        let dx = this.speed * dt * ((this.toX - this.currentX) / d);
        let dy = this.speed * dt * ((this.toY - this.currentY) / d);

        this.me.x += dx;
        this.me.y += dy;

        // console.log(this.me.x + " " + this.me.y)

        Game.instance?.updateMapScalePos()

        // дистанция до конечной точки по данным клиента
        let dc = Math.sqrt(Math.pow(this.toX - this.currentX, 2) + Math.pow(this.toY - this.currentY, 2))
    }
}