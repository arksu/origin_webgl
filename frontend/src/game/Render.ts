import * as PIXI from 'pixi.js'
import { utils } from '@pixi/core'
import type GameData from '@/net/GameData'
import Grid from '@/game/Grid'

export default class Render {

  private readonly canvas: HTMLCanvasElement
  private wasDestroyed: boolean = false

  /**
   * PIXI application
   */
  private readonly app: PIXI.Application

  /**
   * загруженные гриды
   */
  private grids: { [key: string]: Grid } = {}

  /**
   * игровые данные
   */
  public readonly gameData: GameData

  /**
   * контейнер в котором храним контейнеры с гридами и тайлами
   * их координаты внутри абсолютные мировые экранные
   */
  readonly mapGrids: PIXI.Container

  public constructor(data: GameData) {
    console.info('pixi start')
    this.gameData = data

    // создаем канвас для рендера
    this.canvas = document.createElement('canvas')
    this.canvas.id = 'game'
    // но не показываем его, покажем после загрузки всех ресурсов
    this.canvas.style.display = 'none'
    // добавим его в дом дерево
    document.body.appendChild(this.canvas)

    // this.initCanvasHandlers()

    // сглаживание пикселей при масштабировании
    PIXI.TextureStyle.defaultOptions.scaleMode = 'nearest'

    // проверка мобильной версии
    PIXI.isMobile.any

    this.app = new PIXI.Application()
    this.mapGrids = new PIXI.Container()
  }

  init() {
    this.app.init({
      width: window.innerWidth,
      height: window.innerHeight,
      canvas: this.canvas,
      resizeTo: window,
      autoDensity: true,
      preserveDrawingBuffer: true,
      powerPreference: 'high-performance',
      antialias: false,
      backgroundColor: 0x333333
    }).then(() => {
      this.app.ticker.minFPS = 10
      this.app.ticker.maxFPS = 30

      this.app.ticker.add(this.update.bind(this))


    })
  }

  /**
   * загрузить необходимые для старта атласы
   * @private
   */
  load() {
    utils.clearTextureCache()
    utils.destroyTextureCache()

    console.log('load assets...')

    if (!PIXI.Assets.resolver.hasKey('base')) {
      // добавляем в резолвер только 1 раз, при рестарте pixi оно остается в памяти
      PIXI.Assets.add({ alias: 'base', src: '/assets/game/base.json' })
      PIXI.Assets.add({ alias: 'tiles', src: '/assets/game/tiles.json' })
    }

    return PIXI.Assets.load(['base', 'tiles'])
  }

  stop() {
    console.log('pixi stop')
    if (this.wasDestroyed) return
    PIXI.Assets.unload(['base', 'tiles'])

    this.app.destroy({
      removeView: false
    }, {
      children: true
    })

    document.body.removeChild(this.canvas)
    this.wasDestroyed = true
  }

  setup() {
    this.canvas.style.display = 'block'

    // debug
    const s = PIXI.Sprite.from('clock14')
    this.app.stage.addChild(s)

    this.app.stage.addChild(this.mapGrids)
    // console.log(this.mapGrids)

    this.mapGrids.x = -1500
    this.mapGrids.y = -500
    this.mapGrids.scale = 5

  }

  private update(_ticker: PIXI.Ticker): void {
    // console.log('update', ticker)
    // console.log(this.app)
  }

  /**
   * добавить ранее полученный от сервера грид в игру
   */
  public addGrid(x: number, y: number) {
    const gameData = this.gameData
    const k = x + '_' + y
    console.log('render add grid', k)

    // такой грид уже есть и создан
    if (this.grids[k] !== undefined) {
      const g = this.grids[k]
      // сделаем его видимым
      if (!g.visible) {
        g.visible = true
      }
      // а если еще и изменился - перестроим его
      if (gameData.map[k].isChanged) {
        // TODO если изменился тайл в нашем гриде и он на границе.
        //  надо обновить и соседние гриды. т.к. там возможно перекрытие тайлов
        g.rebuild()
      }
    } else {
      // такого грида еще нет - надо создать
      this.grids[k] = new Grid(this, this.mapGrids, x, y)

      // зачистим старые гриды, которые давно уже не видели
      for (const gridsKey in this.grids) {
        const grid = this.grids[gridsKey]
        const playerObject = gameData.playerObject
        if (playerObject !== undefined) {
          // const dist = Math.sqrt(Math.pow(playerObject.x - grid.absoluteX, 2) + Math.pow(playerObject.y - grid.absoluteY, 2))
          //       // дистанция от игрока на которой начнем удалять гриды иэ кэша
          //       const limit = 5 * Tile.FULL_GRID_SIZE
          //       if (!grid.visible && dist > limit) {
          //         grid.destroy()
          //         delete this.grids[gridsKey]
          //         console.warn("old grid delete ", gridsKey)
        }
      }
    }
    // }

    // gameData.map[k].isChanged = false
  }

  /**
   * удалить грид из игры (скрыть его до поры до времени)
   */
  public deleteGrid(x: number, y: number) {
    // for (let gridsKey in this.grids) {
    //   if (this.grids[gridsKey].x == x && this.grids[gridsKey].y == y) {
    //     console.log("delete grid", gridsKey)
    //     this.grids[gridsKey].visible = false
    //     break
    //   }
    // }
  }

}