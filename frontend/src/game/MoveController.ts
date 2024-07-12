import * as PIXI from 'pixi.js'
import type GameObject from '@/game/GameObject'
import type { ObjectMoved, ObjectStopped } from '@/net/packets'
import type Render from '@/game/Render'
import { coordGame2Screen } from '@/game/Tile'

export default class MoveController {
  private readonly render: Render
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

  /**
   * вектор скорости (единичный вектор направления умноженный на скорость)
   * @private
   */
  private vx: number
  private vy: number

  private readonly lineView: PIXI.Graphics

  constructor(render: Render, obj: GameObject, data: ObjectMoved) {
    console.warn('create MoveController')
    this.render = render
    this.me = obj

    this.startX = obj.x
    this.startY = obj.y
    this.serverX = data.x
    this.serverY = data.y
    this.vx = 0
    this.vy = 0
    this.speed = data.s

    this.lineView = new PIXI.Graphics()
    this.render.objectsContainer.addChild(this.lineView)

    this.applyData(data)

    this.render.movingObjects[obj.id] = obj
  }

  public applyData(data: ObjectMoved) {
    // console.log('applyData', data)
    this.serverStopped = false
    this.serverX = data.x
    this.serverY = data.y
    this.toX = data.tx
    this.toY = data.ty
    this.speed = data.s
    this.moveType = data.mt

    const sdx = data.tx - this.me.x
    const sdy = data.ty - this.me.y
    const magnitude = Math.sqrt(sdx * sdx + sdy * sdy)
    this.vx = sdx / magnitude * this.speed
    this.vy = sdy / magnitude * this.speed
    // console.log('vector', this.vx, this.vy)

    const ldx = this.me.x - this.serverX
    const ldy = this.me.y - this.serverY
    const localDelta = Math.sqrt(ldx * ldx + ldy * ldy)

    if (localDelta < 2) {
      this.me.x = this.serverX
      this.me.y = this.serverY
    }

    // рисуем анимацию движения по серверу
    const c1 = coordGame2Screen(this.serverX, this.serverY)
    const c2 = coordGame2Screen(this.toX, this.toY)
    this.lineView.clear()
    this.lineView.moveTo(c1[0], c1[1])
    this.lineView.lineTo(c2[0], c2[1])
    this.lineView.stroke({ width: 2, color: 0x00ff00 })
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
    const ld = Math.sqrt(Math.pow(this.toX - this.me.x, 2) + Math.pow(this.toY - this.me.y, 2))
    console.warn('serverStop ld=' + ld.toFixed(2))
    // if (ld > 5 || ld < 1) {
    if (ld < 1) {
      this.me.x = data.x
      this.me.y = data.y
      console.warn('hard stop ld=' + ld.toFixed(2))
      this.stop()
      this.render.onObjectMoved(this.me)
    }
  }

  public stop() {
    if (this.stopped) {
      console.warn('was stopped')
      return
    }
    console.warn('stop move')

    if (this.serverStopped) {
      this.me.x = this.toX
      this.me.y = this.toY
    }

    delete this.render.movingObjects[this.me.id]
    this.lineView.destroy({ children: true, texture: true })
    this.stopped = true
    this.me.moveController = undefined
  }

  /**
   * обновление движения
   * @param dt в секундах
   */
  public update(dt: number) {
    if (this.stopped) return

    // дистанция до конечной точки по данным сервера
    // local distance
    const dx = this.toX - this.me.x
    const dy = this.toY - this.me.y
    // local distance
    const ld = (dx == 0 && dy == 0) ? 0 : Math.sqrt(dx * dx + dy * dy)

    // если осталось идти слишком мало - посчитаем что уже пришли в назначенную точку
    if (ld <= 2 || Number.isNaN(ld)) {
      this.me.x = this.toX
      this.me.y = this.toY
      // console.warn('stop by low distance', ld)
      this.stop()
    } else {
      const predictedX = this.me.x + this.vx * dt
      const predictedY = this.me.y + this.vy * dt

      const correctDx = this.serverX - predictedX
      const correctDy = this.serverY - predictedY

      const CORRECTION_THRESHOLD = 5
      if (Math.abs(correctDx) > CORRECTION_THRESHOLD || Math.abs(correctDy) > CORRECTION_THRESHOLD || this.serverStopped) {
        const dist = Math.sqrt(correctDx * correctDx + correctDy * correctDy)
        let k = 0.1
        if (dist < 3 || dist > 12) k = 0.3
        if (this.serverStopped) k = 0.3
        // console.warn('correct', correctDx, correctDy, k)
        this.smoothCorrect(this.serverX, this.serverY, k)
      }

      // //  пройдем расстояение не больше чем осталось до конечной точки
      // let nd = Math.min(ld, this.speed * _dt)
      //
      // // сколько прошли
      // const dx = nd * ((this.toX - this.me.x) / ld)
      // const dy = nd * ((this.toY - this.me.y) / ld)
      //
      // //добавим к координатам объекта дельту
      // this.me.x += dx
      // this.me.y += dy


      if (!this.serverStopped) {
        const k = 0.8
        this.me.x = lerp(this.me.x, predictedX, k)
        this.me.y = lerp(this.me.y, predictedY, k)

        // this.me.x += (predictedX - this.me.x) * k
        // this.me.y += (predictedY - this.me.y) * k
      } else {
        // const k = 0.1
        // this.me.x += (this.toX - this.me.x) * k
        // this.me.y += (this.toY - this.me.y) * k
      }
    }

    this.render.onObjectMoved(this.me)
    if (this.render.gameData.selectedCharacterId == this.me.id) this.render?.updateMapScalePos()
  }

  private smoothCorrect(targetX: number, targetY: number, smoothing: number) {
    this.me.x = lerp(this.me.x, targetX, smoothing)
    this.me.y = lerp(this.me.y, targetY, smoothing)
  }

}

function lerp(start: number, end: number, t: number) {
  return start + (end - start) * t
}
