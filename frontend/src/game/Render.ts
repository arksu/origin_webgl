import * as PIXI from 'pixi.js'
import { utils } from '@pixi/core'
import type GameData from '@/net/GameData'
import Grid from '@/game/Grid'
import type GameObject from '@/game/GameObject'
import Tile from '@/game/Tile'
import Point from '@/util/Point'

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
   * масштаб карты и игровой графики
   */
  private scale: number = 1

  /**
   * отступ в координатах экрана от центра экрана до игрока
   */
  private offset: Point = new Point(0, 0)

  /**
   * контейнер в котором храним контейнеры с гридами и тайлами
   * их координаты внутри абсолютные мировые экранные
   */
  readonly mapGrids: PIXI.Container

  /**
   * контейнер для отображения объектов
   */
  public readonly objectsContainer: PIXI.Container

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

    this.initCanvasHandlers()

    // сглаживание пикселей при масштабировании
    PIXI.TextureStyle.defaultOptions.scaleMode = 'nearest'

    // проверка мобильной версии
    PIXI.isMobile.any

    this.app = new PIXI.Application()
    this.mapGrids = new PIXI.Container()

    this.objectsContainer = new PIXI.Container()
    this.objectsContainer.sortableChildren = true
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

  /**
   * все загружено, авторизация на сервере пройдена, инициализируем сцену и начинаем рендер
   */
  setup() {
    this.canvas.style.display = 'block'

    this.app.stage.addChild(this.mapGrids)
    this.app.stage.addChild(this.objectsContainer)

    // пройдем по всем уже полученным объектам и добавим их в игровой мир
    for (const key in this.gameData.objects) {
      this.onObjectAdd(this.gameData.objects[key])
    }

    // обновим положение карты
    this.updateMapScalePos()
  }

  stop() {
    console.log('pixi stop')
    if (this.wasDestroyed) return

    window.removeEventListener('resize', this.resizeHandler)
    window.removeEventListener('orientationchange', this.orientationchangeHandler)
    document.removeEventListener('keydown', this.keydownHandler)

    PIXI.Assets.unload(['base', 'tiles'])

    this.app.destroy({
      removeView: false
    }, {
      children: true
    })

    for (const gridsKey in this.grids) {
      this.grids[gridsKey].destroy()
    }
    this.mapGrids.destroy({ children: true })
    this.objectsContainer.destroy({ children: true })

    document.body.removeChild(this.canvas)
    this.canvas.remove()
    this.wasDestroyed = true
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
          const dist = Math.sqrt(Math.pow(playerObject.x - grid.absoluteX, 2) + Math.pow(playerObject.y - grid.absoluteY, 2))
          // дистанция от игрока на которой начнем удалять гриды иэ кэша
          const limit = 5 * Tile.FULL_GRID_SIZE
          if (!grid.visible && dist > limit) {
            grid.destroy()
            delete this.grids[gridsKey]
            console.warn('old grid delete', gridsKey)
          }
        }
      }
    }

    gameData.map[k].isChanged = false
  }

  /**
   * удалить грид из игры (скрыть его до поры до времени)
   */
  public deleteGrid(x: number, y: number) {
    for (const gridsKey in this.grids) {
      if (this.grids[gridsKey].x == x && this.grids[gridsKey].y == y) {
        console.log('delete grid', gridsKey)
        this.grids[gridsKey].visible = false
        break
      }
    }
  }

  public onObjectAdd(obj: GameObject) {
    // if (obj.view !== undefined) {
    //   obj.view.destroy()
    // }
    //
    // obj.view = new ObjectView(obj)
    // for (let i = 0; i < obj.view.view.length; i++) {
    //   this.objectsContainer.addChild(obj.view.view[i])
    // }
  }

  private getPlayerCoord(): { sx: number, sy: number } {
    const playerObject = this.gameData.playerObject
    if (playerObject !== undefined) {
      const px = playerObject.x
      const py = playerObject.y

      const sx = px / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF - py / Tile.TILE_SIZE * Tile.TILE_WIDTH_HALF
      const sy = px / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF + py / Tile.TILE_SIZE * Tile.TILE_HEIGHT_HALF
      return { sx, sy }
    } else {
      console.warn('no player object')
      return { sx: 0, sy: 0 }
    }
  }

  public updateMapScalePos() {
    const { sx, sy } = this.getPlayerCoord()

    // центр экрана с учетом отступа перетаскиванием
    const offsetX = this.app.renderer.width / 2 + this.offset.x
    const offsetY = this.app.renderer.height / 2 + this.offset.y

    this.mapGrids.x = offsetX - sx * this.scale
    this.mapGrids.y = offsetY - sy * this.scale
    this.objectsContainer.x = offsetX - sx * this.scale
    this.objectsContainer.y = offsetY - sy * this.scale

    this.mapGrids.scale.x = this.scale
    this.mapGrids.scale.y = this.scale
    this.objectsContainer.scale.x = this.scale
    this.objectsContainer.scale.y = this.scale

    // if (this.actionProgress !== undefined) {
    //   this.actionProgress.sprite.x = this.app.renderer.width / 2
    //   this.actionProgress.sprite.y = this.app.renderer.height / 2
    // }
  }

  private onMouseWheel(delta: number) {
    if (this.scale < 1) {
      this.scale += delta / (1000) * (this.scale * 0.5)
    } else {
      this.scale += delta / (1000)
    }
    this.updateScale()
  }

  private updateScale() {
    if (this.scale < 0.05) this.scale = 0.05
    this.updateMapScalePos()
  }

  private onResize() {
    this.updateMapScalePos()
  }

  private resizeTimerId: undefined | number = undefined

  private resizeHandler = () => {
    if (this.resizeTimerId == undefined) {
      this.resizeTimerId = setTimeout(() => {
        this.resizeTimerId = undefined
        this.onResize()
      }, 200)
    }
  }

  // переворот экрана
  private orientationchangeHandler = () => {
    this.onResize()
  }

  private keydownHandler = (e: Event) => {
    if (e.target == document.body && e instanceof KeyboardEvent) {
      this.onKeyDown(e)
    }
  }

  private onKeyDown(e: KeyboardEvent) {
    console.log('onKeyDown:', e.key)
    switch (e.key) {
      case 'Tab':
        // e.preventDefault()
        // this.store.toggleInventory()
        break
      case 'c':
        // e.preventDefault()
        // this.store.craft.isOpened = !this.store.craft.isOpened
        break
      case 'Enter':
        // document.getElementById("inputChat")?.focus();
        break
      case 'Home':
        this.scale = 1
        this.updateScale()
        break
    }
  }

  onMouseRightClick(_e: MouseEvent) {
    // TODO
  }

  /**
   * навешиваем на канвас обработчики
   */
  private initCanvasHandlers() {
    // ловим прокрутку страницы и делаем скейл на основе этого
    this.canvas.addEventListener('wheel', (e: WheelEvent) => {
      this.onMouseWheel(-e.deltaY)
    }, { passive: true })

    // ловим правый клик за счет вызоыва context menu
    this.canvas.addEventListener('contextmenu', (e: Event) => {
      e.preventDefault()
      if (e instanceof MouseEvent) {
        console.log('right click ' + e.x + ' ' + e.y)
        console.log(e.button + ' alt=' + e.altKey + ' shift=' + e.shiftKey + ' meta=' + e.metaKey)
        this.onMouseRightClick(e)
      }
    })

    window.addEventListener('resize', this.resizeHandler)
    window.addEventListener('orientationchange', this.orientationchangeHandler)
    document.addEventListener('keydown', this.keydownHandler)
  }

}