import {GameObject} from "@/game/GameObject";
import Game from "@/game/Game";
import Client from "@/net/Client";
import {ObjectMoved} from "@/net/Packets";

export default class MoveController {

    private readonly me: GameObject

    startX: number
    startY: number

    toX!: number
    toY!: number
    serverX!: number
    serverY!: number
    speed!: number
    moveType!: string

    stopped: boolean = false

    constructor(obj: GameObject, data: ObjectMoved) {
        console.warn("create MoveController")
        this.me = obj

        this.startX = obj.x
        this.startY = obj.y

        this.applyData(data)

        if (Game.instance !== undefined) {
            Game.instance.movingObjects[obj.id] = obj
        }
    }

    public applyData(data: ObjectMoved) {
        this.serverX = data.x
        this.serverY = data.y
        this.toX = data.tx
        this.toY = data.ty
        this.speed = data.s
        this.moveType = data.mt


        // server distance
        let sd = Math.sqrt(Math.pow(this.toX - this.serverX, 2) + Math.pow(this.toY - this.serverY, 2))
        // local distance
        let ld = Math.sqrt(Math.pow(this.toX - this.me.x, 2) + Math.pow(this.toY - this.me.y, 2))

        // корректировка скорости
        let diff = Math.abs(sd - ld)
        if (sd > 2 && (diff > 2)) {
            console.warn("speed correct")
            let k = ld / sd
            k = Math.max(0.4, k)
            k = Math.min(1.4, k)
            this.speed = this.speed * k
        }
        console.log("diff=" + diff.toFixed(2) + " ld=" + ld.toFixed(2) + " sd=" + sd.toFixed(2) + " speed=" + this.speed.toFixed(2))
    }

    public stop() {
        if (this.stopped) return
        console.warn("stop move")
        if (Game.instance !== undefined) {
            delete Game.instance.movingObjects[this.me.id]
        }
        this.stopped = true
    }

    public update(dt: number) {
        // console.log("dt=" + Math.round(dt * 1000))
        // console.log(this)
        // дистанция до конечной точки по данным сервера
        // server distance
        let sd = Math.sqrt(Math.pow(this.toX - this.serverX, 2) + Math.pow(this.toY - this.serverY, 2))
        // local distance
        let ld = Math.sqrt(Math.pow(this.toX - this.me.x, 2) + Math.pow(this.toY - this.me.y, 2))

        // console.log("ld="+ld)
        // если осталось идти слишком мало - посчитаем что уже пришли в назначенную точку
        if (ld <= 1) {
            this.me.x = this.toX
            this.me.y = this.toY
            this.stop()
        // } else if (Math.abs(sd - ld) > 5) {
        //     this.me.x = this.serverX
        //     this.me.y = this.serverY
        } else {
            // пройдем расстояение не больше чем осталось до конечной точки
            let nd = Math.min(ld, this.speed * dt)

            // сколько прошли
            let dx = nd * ((this.toX - this.serverX) / sd);
            let dy = nd * ((this.toY - this.serverY) / sd);

            // добавим к координатам объекта дельту
            this.me.x += dx;
            this.me.y += dy;
        }
        Game.instance?.onObjectMoved(this.me)
        if (Client.instance.selectedCharacterId == this.me.id) Game.instance?.updateMapScalePos()
    }
}
