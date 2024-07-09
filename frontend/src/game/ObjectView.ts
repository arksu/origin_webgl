import * as PIXI from 'pixi.js'
import { FederatedPointerEvent } from 'pixi.js'
import type GameObject from '@/game/GameObject'
import type Coord from '@/util/Coord'
import { coordGame2Screen } from '@/game/Tile'
import objects from './objects.json'
import type Render from '@/game/Render'
import Point from '@/util/Point'
import { ClientPacket } from '@/net/packets'
import { getKeyFlags } from '@/util/keyboard'

export interface Layer {
  img: string
  interactive?: boolean
  offset?: Coord
  z?: number
  shadow?: boolean
}

export interface Resource {
  size?: Coord
  offset?: Coord
  layers: Layer[]
}

/**
 * внешнее представление объекта в игре
 */
export default class ObjectView {
  readonly render: Render
  readonly obj: GameObject
  container = new PIXI.Container()
  sprites: PIXI.Sprite[] = []

  isDestroyed: boolean = false
  private readonly res: Resource
  private touchTimer: number = -1
  private wasRightClick: boolean = false

  constructor(obj: GameObject, render: Render) {
    this.obj = obj
    this.render = render

    this.res = this.getResource(obj.r)
    if (this.res == undefined) {
      this.res = this.getResource('unknown')
    }
    this.makeLayers()
    this.onMoved()
  }

  /**
   * получить ресурс объекта из пути
   */
  private getResource(r: string): Resource {
    const strings = r.split('/')

    // берем первый уровень вложенности
    // @ts-ignore
    let res = <Resource>objects[strings[0]]
    if (res == undefined) return res

    // идем по всем последующим
    for (let i = 1; i < strings.length; i++) {
      // @ts-ignore
      res = res[strings[i]]
      if (res == undefined) return res
    }
    return res
  }

  public makeLayers() {
    // идем по слоям
    for (let i = 0; i < this.res.layers.length; i++) {
      this.addLayer(this.res.layers[i])
    }
    this.isDestroyed = false
  }

  private addLayer(l: Layer) {
    let path = l.img
    // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
    if (path.includes('.')) path = '/assets/game/' + path

    if (!PIXI.Assets.cache.has(path)) {
      PIXI.Assets.load(path).then((t) => {
        console.log('loaded', path)
        // создаем спрайт из текстуры которую загрузили
        this.makeSprite(t, l)
      })
    } else {
      // создаем спрайт для каждого слоя
      this.makeSprite(path, l)
    }
  }

  private makeSprite(src: any, l: Layer): void {
    const spr = PIXI.Sprite.from(src)
    if (l.interactive) this.setInteractive(spr)
    if (l.shadow) {
      spr.zIndex = -1
    }

    // оффсет слоя
    if (l.offset != undefined) {
      spr.x = l.offset[0]
      spr.y = l.offset[1]
    }

    // добавим оффсет для объекта
    if (this.res.offset != undefined) {
      spr.x -= this.res.offset[0]
      spr.y -= this.res.offset[1]
    }
    // z = y
    let z = this.container.y
    if (l.shadow) {
      z = -1
    }
    // TODO sort z index
    // spr.zIndex = z

    this.sprites.push(spr)
    this.container.addChild(spr)
  }

  public destroy() {
    if (!this.isDestroyed) {
      for (let i = 0; i < this.sprites.length; i++) {
        this.sprites[i].destroy()
      }
      this.container.destroy()
      this.isDestroyed = true
    }
  }

  public onMoved() {
    const coord = coordGame2Screen(this.obj.x, this.obj.y)
    this.container.x = coord[0]
    this.container.y = coord[1]
  }

  /**
   * сервер уведомил о том что изменился файл в ассетах
   * @param f путь до текстуры
   */
  public onAssetsChanged(f: string) {
    // идем по слоям
    for (let i = 0; i < this.res.layers.length; i++) {
      if (this.res.layers[i].img == f) {
        const l = this.res.layers[i]
        let path = l.img
        // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
        if (path.includes('.')) path = 'assets/game/' + path + '?' + (+new Date())

        this.sprites[i].texture = PIXI.Texture.from(path)
      }
    }
  }

  private setInteractive(target: PIXI.Sprite | PIXI.Container) {
    // console.log('setInteractive', target)
    target.interactive = true
    target.onrightclick = (e: FederatedPointerEvent) => {
      this.onRightClick(e)
    }
    target.onmousedown = (e: FederatedPointerEvent) => {
      this.onClick(e)
    }
    /*
     target.on("touchstart", (e: PIXI.InteractionEvent) => {
       this.isTouched = true
       // this.touchTimer = setTimeout(() => {
       //     if (this.isTouched) {
       //         this.isTouched = false
       //         // укажем что был правый клик (сработал)
       //         this.wasRightClick = true
       //         console.log("rightclick")
       //         this.onRightClick(e)
       //
       //         clearTimeout(this.touchTimer)
       //         this.touchTimer = -1
       //     }
       // }, 800)
     })
     target.on("touchend", (e: PIXI.InteractionEvent) => {
       this.isTouched = false
       if (this.touchTimer != -1) {
         clearTimeout(this.touchTimer)
         this.touchTimer = -1
       }
       // только если не сработал правый клик - выполним основной клик
       if (!this.wasRightClick) {
         this.onClick(e)
       }
       // и в любом случае затрем флаг правого клика
       this.wasRightClick = false
     })
     target.on("touchendoutside", () => {
       this.isTouched = false
       if (this.touchTimer != -1) {
         clearTimeout(this.touchTimer)
         this.touchTimer = -1
       }
     })
     target.on("mousedown", (e: PIXI.InteractionEvent) => {
       this.onClick(e)
     })

     */
  }

  private onClick(e: PIXI.FederatedPointerEvent) {
    // screen point coord
    const p = new Point(e.screen).round()
    // вычислим игровые координаты куда тыкнула мышь
    // их тоже отправим на сервер
    const cp = this.render.coordScreen2Game(p)

    this.render.client.send(ClientPacket.OBJECT_CLICK, {
      id: this.obj.id,
      f: getKeyFlags(e),
      x: cp.x,
      y: cp.y
    })
  }

  private onRightClick(e: PIXI.FederatedPointerEvent) {

    console.log('onrightclick', e)
    const p = new Point(e.global).round()
    const cp = this.render.coordScreen2Game(p)

    this.render.client.send(ClientPacket.OBJECT_RIGHT_CLICK, {
      id: this.obj.id,
      x: cp.x,
      y: cp.y
    })

  }
}