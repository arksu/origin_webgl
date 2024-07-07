import * as PIXI from 'pixi.js'
import type GameObject from '@/game/GameObject'
import type Coord from '@/util/Coord'
import { coordGame2Screen } from '@/game/Tile'
import objects from './objects.json'

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
  readonly obj: GameObject
  container = new PIXI.Container()
  view: PIXI.Sprite[] = []

  isDestroyed: boolean = false
  private readonly res: Resource
  private layersOffset: Coord[] = []
  private touchTimer: number = -1
  private wasRightClick: boolean = false

  constructor(obj: GameObject) {
    this.obj = obj

    this.res = this.getResource(obj.r)
    if (this.res == undefined) {
      this.res = this.getResource('unknown')
    }
    this.makeLayers()
    this.setSpritePositions()
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

    // если есть оффсет надо его проставить
    if (l.offset != undefined) {
      this.layersOffset.push(l.offset)
    } else {
      this.layersOffset.push([0, 0])
    }

    if (!PIXI.Assets.cache.has(path)) {
      PIXI.Assets.load(path).then((t) => {
        console.log('loaded', path)
        // создаем спрайт для каждого слоя
        const spr = PIXI.Sprite.from(t)
        console.log(spr)
        if (l.interactive) this.setInteractive(spr)
        if (l.shadow) {
          spr.zIndex = -1
        }
        this.view.push(spr)
        this.container.addChild(spr)

        // TODO : проставлять только для этого спрайта
        this.setSpritePositions()
      })
    } else {

      // создаем спрайт для каждого слоя
      const spr = PIXI.Sprite.from(path)
      if (l.interactive) this.setInteractive(spr)
      if (l.shadow) {
        spr.zIndex = -1
      }
      this.view.push(spr)
      this.container.addChild(spr)
    }
  }

  public destroy() {
    if (!this.isDestroyed) {
      for (let i = 0; i < this.view.length; i++) {
        this.view[i].destroy()
      }
      this.container.destroy()
      this.isDestroyed = true
    }
  }

  private setSpritePositions() {
    console.log('setSpritePositions')
    for (let i = 0; i < this.view.length; i++) {
      // оффсет слоя
      this.view[i].x =  this.layersOffset[i][0]
      this.view[i].y =  this.layersOffset[i][1]

      // добавим оффсет для объекта
      if (this.res.offset != undefined) {
        this.view[i].x -= this.res.offset[0]
        this.view[i].y -= this.res.offset[1]
      }
      // z = y
      let z = this.container.y
      if (this.res.layers[i].shadow) {
        z = -1
      }
      this.view[i].zIndex = z
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

        this.view[i].texture = PIXI.Texture.from(path)
      }
    }
  }

  private setInteractive(target: PIXI.Sprite | PIXI.Container) {
    /*
     target.interactive = true
     target.on("rightclick", (e: PIXI.InteractionEvent) => {
       this.onRightClick(e)
     })
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

  private onClick(e: PIXI.FederatedEvent) {
    /*
    // screen point coord
    const p = new Point(e.data.global).round();
    if (Render.instance !== undefined) {
      // вычислим игровые координаты куда тыкнула мышь
      // их тоже отправим на сервер
      let cp = Render.instance.coordScreen2Game(p);

      GameClient.remoteCall("objclick", {
        id: this.obj.id,
        f: getKeyFlags(e),
        x: cp.x,
        y: cp.y
      })
    }

     */
  }

  private onRightClick(e: PIXI.FederatedEvent) {
    /*
    console.log(e)
    if (Render.instance != undefined) {
      const p = new Point(e.data.global).round();
      let cp = Render.instance.coordScreen2Game(p);

      GameClient.remoteCall("objrclick", {
        id: this.obj.id,
        x: cp.x,
        y: cp.y
      })
    }

     */
  }
}