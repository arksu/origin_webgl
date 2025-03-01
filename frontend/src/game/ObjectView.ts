import * as PIXI from 'pixi.js'
import { Spine } from '@esotericsoftware/spine-pixi-v8'
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
  // sprite image
  img?: string
  // spine animation
  spine?: SpineData
  // can click?
  interactive?: boolean
  // set sprite offset
  offset?: Coord
  z?: number
  // this is shadow layer? put it on z=-1
  shadow?: boolean
}

type SpineData = {
  file: string
  scale?: number
  skin?: string
  // move directions (0-7)
  dirs?: Directions
}

type Directions = {
  [key: string]: string[]
}

// export interface Directions {
//   walk: string[]
//   idle : string[]
// }

export interface Resource {
  layers: Layer[]
  size?: Coord
  offset?: Coord
}

/**
 * внешнее представление объекта в игре
 */
export default class ObjectView {
  readonly render: Render
  readonly obj: GameObject
  container = new PIXI.Container()
  sprites: PIXI.Sprite[] = []
  spineAnimations : Spine[] = []

  isDestroyed: boolean = false
  private readonly res: Resource
  private touchTimer: number = -1
  private wasRightClick: boolean = false
  private lastDir : number = 4

  constructor(obj: GameObject, render: Render) {
    this.obj = obj
    this.render = render

    this.res = this.getResource(obj.r)
    if (this.res == undefined) {
      this.res = this.getResource('unknown')
      if (this.res == undefined) {
        console.error('<unknown> resource not found')
      }
    }
    this.makeLayers()
    this.onStopped()
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
    // создание спрайтов
    if (l.img !== undefined) {
      let path = l.img
      // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
      if (path.includes('.')) path = '/assets/game/' + path

      // ищем в кэеше ассетов
      if (!PIXI.Assets.cache.has(path)) {
        // в кэше нет - надо загрузить
        PIXI.Assets.load(path).then((t) => {
          console.log('loaded', path)
          // после загрузки создаем спрайт из текстуры
          this.makeSprite(t, l)
        })
      } else {
        // спрайти в кэше есть
        // создаем спрайт для каждого слоя
        this.makeSprite(path, l)
      }
    }

    // загружаем Spine
    if (l.spine !== undefined) {
      const path = '/assets/game/' + l.spine.file
      if (!PIXI.Assets.cache.has(path)) {
        const data = l.spine.file + '-data'
        const atlas = l.spine.file + '-atlas'
        PIXI.Assets.add({ alias: data, src: path + '.json' })
        PIXI.Assets.add({ alias: atlas, src: path + '.atlas' })
        PIXI.Assets.load([data, atlas]).then((spine) => {
          console.log("spine loaded", spine)
          this.makeSpine(data, atlas, l)
        })
      }
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
    spr.zIndex = z
    // spr.setChildIndex()

    this.sprites.push(spr)
    this.container.addChild(spr)
  }

  private makeSpine(data: string, atlas: string, l: Layer) {
    // https://ru.esotericsoftware.com/spine-pixi#Loading-Spine-Assets
    console.log("makeSpine")
    const spineAnimation = Spine.from({ skeleton: data, atlas: atlas, autoUpdate: true })
    console.log(spineAnimation)

    spineAnimation.x = 0
    spineAnimation.y = 0

    // оффсет слоя
    if (l.offset != undefined) {
      spineAnimation.x = l.offset[0]
      spineAnimation.y = l.offset[1]
    }
    // добавим оффсет для объекта
    if (this.res.offset != undefined) {
      spineAnimation.x -= this.res.offset[0]
      spineAnimation.y -= this.res.offset[1]
    }
    if (l.spine?.scale != undefined) {
      spineAnimation.scale = l.spine?.scale
    }
    if (l.spine?.skin !== undefined) {
      spineAnimation.skeleton.setSkinByName(l.spine.skin)
    }

    // TODO
    spineAnimation.state.setAnimation(0, "s-idle", true)
    
    this.spineAnimations.push(spineAnimation)
    this.container.addChild(spineAnimation)
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

  public onMoved(dir : number) {
    //console.warn("onMoved", dir)
    const coord = coordGame2Screen(this.obj.x, this.obj.y)
    this.container.x = coord[0]
    this.container.y = coord[1]
    this.lastDir = dir

    this.res.layers.forEach((l, idx) => {
        if (l.spine !== undefined) {
          // console.warn("layer", l, idx)
          // console.log(this.spineAnimations)
          let d = l.spine?.dirs?.["walk"][dir]
          // console.warn("setanimation", dir, d)
          if (d !== undefined) {
            const anim = this.spineAnimations[idx]
            if (anim !== undefined) {
              const current = anim.state.getCurrent(0)?.animation?.name
              // console.warn(anim.state.getCurrent(0)?.animation?.name)
              if (current !== d) {
            anim.state.setAnimation(0,d, true)
              }
            }
          }
        }
    })
  }

  public onStopped() {
    console.warn("onStopped")
    const coord = coordGame2Screen(this.obj.x, this.obj.y)
    this.container.x = coord[0]
    this.container.y = coord[1]

    this.res.layers.forEach((l, idx) => {
      if (l.spine !== undefined) {
        console.warn("layer", l, idx)
        console.log(this.spineAnimations)
        let d = l.spine?.dirs?.["idle"][this.lastDir]
        console.warn("onStopped", this.lastDir, d)
        if (d !== undefined) {
          const anim = this.spineAnimations[idx]
          if (anim !== undefined) {
            //const current = anim.state.getCurrent(0)?.animation?.name
            console.warn("current", anim.state.getCurrent(0)?.animation?.name)
            //if (current !== d) {
              anim.state.setAnimation(0,d, true)
            //}
          }
        }
      }
  })
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
        if (l.img !== undefined) {
          let path = l.img
          // если в пути до картинки есть точка (расширение файла) то грузим из ассетов (иначе это элемент атласа)
          if (path.includes('.')) path = 'assets/game/' + path + '?' + (+new Date())

          this.sprites[i].texture = PIXI.Texture.from(path)
        }
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
      this.onMouseDown(e)
    }
    target.onmousemove = (e: FederatedPointerEvent) => {
      this.render.onMouseMove(e)
    }
    target.onmouseup = (e: FederatedPointerEvent) => {
      this.render.onMouseUp(e)
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

  private onMouseDown(e: PIXI.FederatedPointerEvent) {
    if (e.buttons == 1) {
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
    } else {
      this.render.onMouseDown(e)
    }
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