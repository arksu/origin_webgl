import * as PIXI from 'pixi.js';
import GameObject from "./GameObject";
import {ObjectMoved, ObjectStopped} from "../net/packets";
import Render from "./Render";
import GameClient from "../net/GameClient";

export default class MoveController {

    private readonly me: GameObject

    private readonly startX: number
    private readonly startY: number

    private toX!: number
    private toY!: number
    private serverX!: number
    private serverY!: number
    private speed!: number
    private moveType!: string

    private stopped: boolean = false
    private serverStopped: boolean = false

    private readonly lineView: PIXI.Graphics

    constructor(obj: GameObject, data: ObjectMoved) {
        console.warn("create MoveController")
        this.me = obj

        this.startX = obj.x
        this.startY = obj.y
        this.serverX = data.x
        this.serverY = data.y

        this.lineView = new PIXI.Graphics()
        Render.instance?.objectsContainer.addChild(this.lineView)

        this.applyData(data)

        if (Render.instance !== undefined) {
            Render.instance.movingObjects[obj.id] = obj
        }
    }

    public applyData(data: ObjectMoved) {
        this.serverStopped = false
        this.serverX = data.x
        this.serverY = data.y
        this.toX = data.tx
        this.toY = data.ty
        this.speed = data.s
        this.moveType = data.mt

        let ldx = this.me.x - this.serverX
        let ldy = this.me.y - this.serverY
        let lds = Math.sqrt(ldx * ldx + ldy * ldy)

        if (lds > 2) {
            // this.me.x = this.serverX
            // this.me.y = this.serverY
        }

        // server distance
        let sd = Math.sqrt(Math.pow(this.toX - this.serverX, 2) + Math.pow(this.toY - this.serverY, 2))
        // local distance
        let ld = Math.sqrt(Math.pow(this.toX - this.me.x, 2) + Math.pow(this.toY - this.me.y, 2))

        // let c1 = Game.coordGame2Screen(this.me.x, this.me.y)
        let c1 = Render.coordGame2Screen(this.serverX, this.serverY)
        let c2 = Render.coordGame2Screen(this.toX, this.toY)
        this.lineView.clear().lineStyle(2, 0x00ff00).moveTo(c1[0], c1[1]).lineTo(c2[0], c2[1])

        // корректировка скорости
        // let diff = Math.abs(sd - ld)
        // if (sd > 2 && (diff > 1)) {
        //     let k = ld / sd
        //     // ограничиваем максимальную и минимальную корректировку, чтобы сильно не дергалось
        //     k = Math.max(0.4, k)
        //     k = Math.min(1.4, k)
        //     console.warn("speed correct k=" + k.toFixed(2))
        //     this.speed = this.speed * k
        // }
        // console.log("diff=" + diff.toFixed(2) + " ld=" + ld.toFixed(2) + " sd=" + sd.toFixed(2) + " speed=" + this.speed.toFixed(2))
    }

    /**
     * сервер говорит что объект остановился
     */
    public serverStop(data: ObjectStopped) {
        this.toX = data.x
        this.toY = data.y
        this.serverX = data.x
        this.serverY = data.y
        this.serverStopped = true

        // local distance
        let ld = Math.sqrt(Math.pow(this.toX - this.me.x, 2) + Math.pow(this.toY - this.me.y, 2))
        console.warn("serverStop ld=" + ld.toFixed(2))
        // if (ld > 5 || ld < 1) {
        if (ld < 1) {
            this.me.x = data.x
            this.me.y = data.y
            console.warn("hard stop ld=" + ld.toFixed(2))
            this.stop()
            Render.instance?.onObjectMoved(this.me)
        }

        // this.lineView.destroy({children: true, texture: true})
    }

    public stop() {
        if (this.stopped) {
            console.warn("was stopped")
            return
        }
        console.warn("stop move")

        if (this.serverStopped) {
            this.me.x = this.toX
            this.me.y = this.toY
        }

        if (Render.instance !== undefined) {
            delete Render.instance.movingObjects[this.me.id]
        }
        this.lineView.destroy({children: true, texture: true})
        this.stopped = true
        this.me.moveController = undefined
    }

    public update(dt: number) {
        if (this.stopped) return
        // if (this.serverStopped) console.log("dt=" + Math.round(dt * 1000))
        // if (this.serverStopped) console.log(this)

        // дистанция до конечной точки по данным сервера
        // server distance
        // let sd = Math.sqrt(Math.pow(this.toX - this.serverX, 2) + Math.pow(this.toY - this.serverY, 2))
        // local distance
        let dx = this.toX - this.me.x
        let dy = this.toY - this.me.y
        let ld = (dx == 0 && dy == 0) ? 0 : Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))

        // if (this.serverStopped) console.log("ld=" + ld)
        // если осталось идти слишком мало - посчитаем что уже пришли в назначенную точку
        if (ld <= 1 || Number.isNaN(ld)) {
            this.me.x = this.toX
            this.me.y = this.toY
            console.warn("stop by low distance")
            this.stop()
        } else {
            // пройдем расстояение не больше чем осталось до конечной точки
            let nd = Math.min(ld, this.speed * dt)

            // сколько прошли
            let dx = nd * ((this.toX - this.me.x) / ld);
            let dy = nd * ((this.toY - this.me.y) / ld);

            // добавим к координатам объекта дельту
            //  this.me.x += dx;
            //  this.me.y += dy;

            // let k = this.speed / 1000
            let k = 0.08
            this.me.x += (this.serverX - this.me.x) * k;
            this.me.y += (this.serverY - this.me.y) * k;
            // console.log(this.me.x, this.me.y)
        }

        Render.instance?.onObjectMoved(this.me)
        if (GameClient.data.selectedCharacterId == this.me.id) Render.instance?.updateMapScalePos()
    }
}
