import type { FederatedPointerEvent } from 'pixi.js'
import * as PIXI from 'pixi.js'
import { utils } from '@pixi/core'
import type GameData from '@/net/GameData'
import Grid from '@/game/Grid'
import type GameObject from '@/game/GameObject'
import Tile from '@/game/Tile'
import Point from '@/util/Point'
import ObjectView from '@/game/ObjectView'
import type GameClient from '@/net/GameClient'
import { ClientPacket } from '@/net/packets'
import { getKeyFlags } from '@/util/keyboard'
import { isButtonMiddle, isButtonPrimary } from '@/util/mouse'
import { useGameStore } from '@/stores/gameStore'
import type Coord from '@/util/Coord'

export default class Render {

  private readonly canvas: HTMLCanvasElement
  private wasDestroyed: boolean = false
  private isInitialized: boolean = false

  /**
   * PIXI application
   */
  private readonly app: PIXI.Application

  /**
   * загруженные гриды
   */
  private grids: { [key: string]: Grid } = {}

  /**
   * список объектов которые в данный момент движутся
   * апдейтим их позицию каждый тик через move controller
   */
  public movingObjects: { [key: number]: GameObject } = {}

  /**
   * игровые данные
   */
  public readonly gameData: GameData

  private _client ?: GameClient

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
  readonly mapGridsContainer: PIXI.Container

  /**
   * контейнер для отображения объектов
   */
  public readonly objectsContainer: PIXI.Container

  /**
   * контейнер для перехвата событий ввода от пользователя
   */
  public readonly screenContainer: PIXI.Sprite

  /**
   * "перетаскивание" карты мышью
   */
  private dragStart ?: Point
  /**
   * какой был отступ карты в начале перетаскивания
   */
  private dragOffset ?: Point
  private dragMoved: boolean = false

  /**
   * прикосновения к экрану
   * для обработки мультитача для скейла на мобилках
   */
  private touchCurrent: { [key: number]: Point } = {}

  /**
   * текущее расстояние между пальцами при скейле на мобилках
   */
  private touchLength: number = -1

  /**
   * таймер для непрерывного движения пока нажата ЛКМ
   */
  private continuousMovingTimer: number = -1

  /**
   * последние экранные координаты при нажатой ЛКМ
   */
  private lastMoveCoord ?: Point

  private readonly store = useGameStore()

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
    this.mapGridsContainer = new PIXI.Container()
    this.objectsContainer = new PIXI.Container()
    this.screenContainer = new PIXI.Sprite()
  }

  init() {
    this.app.init({
      width: window.innerWidth,
      height: window.innerHeight,
      canvas: this.canvas,
      resizeTo: window,
      autoDensity: true,
      preserveDrawingBuffer: true,
      // powerPreference: 'high-performance',
      antialias: false,
      backgroundColor: 0x333333
    }).then(() => {
      this.isInitialized = true
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

    this.app.stage.addChild(this.mapGridsContainer)
    this.app.stage.addChild(this.screenContainer)
    this.app.stage.addChild(this.objectsContainer)

    this.screenContainer.x = 0
    this.screenContainer.y = 0
    this.screenContainer.width = this.app.renderer.width
    this.screenContainer.height = this.app.renderer.height
    this.screenContainer.interactive = true
    console.log(this.screenContainer)
    console.log(this.app.renderer)

    this.screenContainer.onmousedown = (e: FederatedPointerEvent) => {
      // this.onMouseDown(e)
    }
    this.screenContainer.onmouseup = (e: FederatedPointerEvent) => {
      // this.onMouseUp(e)
    }
    this.screenContainer.onmousemove = (e: FederatedPointerEvent) => {
      this.onMouseMove(e)
    }
    this.screenContainer.onpointerdown = (e: FederatedPointerEvent) => {
      console.log('onpointerdown', e)
      this.onMouseDown(e)
    }
    this.screenContainer.onpointerup = (e: FederatedPointerEvent) => {
      this.onMouseUp(e)
    }
    this.screenContainer.onpointermove = (e: FederatedPointerEvent) => {
      this.onMouseMove(e)
    }

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


    if (this.isInitialized) {
      PIXI.Assets.unload(['base', 'tiles'])

      this.app.destroy({
        removeView: false
      }, {
        children: true
      })

      for (const gridsKey in this.grids) {
        this.grids[gridsKey].destroy()
      }
      this.mapGridsContainer.destroy({ children: true })
      this.objectsContainer.destroy({ children: true })
    }

    document.body.removeChild(this.canvas)
    this.canvas.remove()
    this.wasDestroyed = true
  }

  public get client(): GameClient {
    if (this._client == undefined) throw new Error('game client in render is not defined')
    return this._client
  }

  public set client(client: GameClient) {
    if (this._client !== undefined) throw new Error('game client in render is already defined')
    this._client = client
  }

  private update(_ticker: PIXI.Ticker): void {
    if (this.wasDestroyed) return

    // delta time in seconds
    // важно. берем elapsedMS т.к. у нас сервер управляет движением. и нам надо абсолютное время ни от чего не зависящее
    const dt = _ticker.elapsedMS / 1000

    // изменились размеры окна
    if (this.app.renderer.width !== this.screenContainer.width || this.app.renderer.height !== this.screenContainer.height) {
      this.onResize()
    }

    // передвигаем все движущиеся объекты
    let cnt = 0
    for (const key in this.movingObjects) {
      const moveController = this.movingObjects[key].moveController
      if (moveController !== undefined) {
        moveController.update(dt)
      }
      cnt++
    }
    // если передвинулся хотя бы 1 объект на экране - надо пересортировать Z Order
    if (cnt > 0) {
      this.sortObjects()
    }
  }

  /**
   * добавить ранее полученный от сервера грид в игру
   */
  public addGrid(x: number, y: number) {
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
      if (this.gameData.map[k].isChanged) {
        // TODO если изменился тайл в нашем гриде и он на границе.
        //  надо обновить и соседние гриды. т.к. там возможно перекрытие тайлов
        g.rebuild()
      }
    } else {
      // такого грида еще нет - надо создать
      this.grids[k] = new Grid(this, this.mapGridsContainer, x, y)

      // зачистим старые гриды, которые давно уже не видели
      for (const gridsKey in this.grids) {
        const grid = this.grids[gridsKey]
        const playerObject = this.gameData.playerObject
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

    this.gameData.map[k].isChanged = false
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
    if (obj.view !== undefined) {
      obj.view.destroy()
    }

    obj.view = new ObjectView(obj, this)
    this.objectsContainer.addChild(obj.view.container)
    this.sortObjects()
  }

  public onObjectDelete(obj: GameObject) {
    if (obj.view !== undefined) {
      obj.view.destroy()
      this.sortObjects()
    }
  }

  public onObjectMoved(obj: GameObject) {
    obj.view?.onMoved()
  }

  public sortObjects() {
    this.objectsContainer.children.sort((a, b) => a.position.y - b.position.y)

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
      return { sx: 0, sy: 0 }
    }
  }

  /**
   * перевести экранные координаты в игровые
   */
  public coordScreen2Game(p: Point): Point {
    p.dec(this.offset)

    const playerObject = this.gameData.playerObject
    if (playerObject == undefined) {
      console.warn('coordScreen2Game no player object')
      return new Point(0, 0)
    }
    const px = playerObject.x
    const py = playerObject.y

    // console.log('player pos ' + px + ' ' + py)

    const screenWidthHalf = this.app.renderer.width / 2
    const screenHeightHalf = this.app.renderer.height / 2
    p.decValue(screenWidthHalf, screenHeightHalf).div(this.scale)

    return new Point(
      p.y / Tile.TEXTURE_HEIGHT + p.x / Tile.TEXTURE_WIDTH,
      p.y / Tile.TEXTURE_HEIGHT - p.x / Tile.TEXTURE_WIDTH)
      .mul(Tile.TILE_SIZE)
      .incValue(px, py)
      .round()
  }

  public coordGame2ScreenAbs(x: number, y: number): Coord {
    const { sx, sy } = this.getPlayerCoord()

    const cx = x / Tile.TILE_SIZE
    const cy = y / Tile.TILE_SIZE
    const ax = cx * Tile.TILE_WIDTH_HALF - cy * Tile.TILE_WIDTH_HALF
    const ay = cx * Tile.TILE_HEIGHT_HALF + cy * Tile.TILE_HEIGHT_HALF


    // центр экрана с учетом отступа перетаскиванием
    const offsetX = this.app.renderer.width / 2 + this.offset.x
    const offsetY = this.app.renderer.height / 2 + this.offset.y
    console.log('coord', ax, ay)
    console.log('offx=' + offsetX + ' offy=' + offsetY)
    return [offsetX - (sx - ax) * this.scale, offsetY - (sy - ay) * this.scale]
  }

  public updateMapScalePos() {
    const { sx, sy } = this.getPlayerCoord()

    // центр экрана с учетом отступа перетаскиванием
    const offsetX = this.app.renderer.width / 2 + this.offset.x
    const offsetY = this.app.renderer.height / 2 + this.offset.y

    this.mapGridsContainer.x = offsetX - sx * this.scale
    this.mapGridsContainer.y = offsetY - sy * this.scale
    this.objectsContainer.x = offsetX - sx * this.scale
    this.objectsContainer.y = offsetY - sy * this.scale

    this.mapGridsContainer.scale.x = this.scale
    this.mapGridsContainer.scale.y = this.scale
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

  onMouseDown(e: PIXI.FederatedPointerEvent) {
    console.log('onMouseDown buttons', e.buttons)
    // this.touchCurrent[e.data.identifier] = new Point(e.data.global)

    // двигаем карту если средняя кнопка мыши или мобильное устройство
    if (isButtonMiddle(e) || PIXI.isMobile.any) {
      this.dragStart = new Point(e.screen).round()
      this.dragOffset = new Point(this.offset)
      console.log('onMouseDown drag start ' + this.dragStart.toString())
    } else if (isButtonPrimary(e) && !PIXI.isMobile.any) {
      const p = new Point(e.screen).round()
      this.lastMoveCoord = new Point(e.screen).round()
      // это просто клик. без сдвига карты
      const cp = this.coordScreen2Game(p)

      console.log('mapclick ' + cp.toString())
      this.client.send(ClientPacket.MAP_CLICK, {
        b: e.button,
        f: getKeyFlags(e),
        x: cp.x,
        y: cp.y
      })

      if (this.continuousMovingTimer !== -1) {
        clearInterval(this.continuousMovingTimer)
      }

      // запускаем таймер который периодически шлет клики по карте на сервер в текущие экранные координаты
      this.continuousMovingTimer = window.setInterval(() => {
        if (this.lastMoveCoord !== undefined) {
          console.log('lastMoveCoord', this.lastMoveCoord)
          const cp = this.coordScreen2Game(this.lastMoveCoord.clone())

          console.log('mapclick ' + cp.toString())
          this.client.send(ClientPacket.MAP_CLICK, {
            b: e.button,
            f: getKeyFlags(e),
            x: cp.x,
            y: cp.y
          })
        }
      }, 333)
    }
    this.dragMoved = false
  }

  onMouseUp(e: PIXI.FederatedPointerEvent) {
    // delete this.touchCurrent[e.data.identifier]

    // this.touchLength = -1

    // screen point coord
    const p = new Point(e.screen).round()

    console.log('onMouseUp buttons ' + e.buttons)

    // если сдвинули карту при нажатии мыши
    if (this.dragStart !== undefined && this.dragOffset !== undefined) {
      const d = new Point(p).dec(this.dragStart)

      // мышь передвинулась достаточно далеко?
      if (Math.abs(d.x) > 10 || Math.abs(d.y) > 10 || this.dragMoved) {
        this.offset.set(this.dragOffset).inc(d)

        this.updateMapScalePos()
      } else {
        // иначе это был просто клик
        const cp = this.coordScreen2Game(p)
        console.log('mapclick ' + cp.toString())
        this.client.send(ClientPacket.MAP_CLICK, {
          b: e.button,
          f: getKeyFlags(e),
          x: cp.x,
          y: cp.y
        })
      }
      this.dragStart = undefined
      this.dragOffset = undefined
      this.touchCurrent = {}
    }
    // выключим таймер непрерывного движения
    if (this.continuousMovingTimer !== -1) {
      clearInterval(this.continuousMovingTimer)
    }
    this.continuousMovingTimer = -1
    this.lastMoveCoord = undefined
  }

  onMouseMove(e: PIXI.FederatedPointerEvent) {
    const keys = Object.keys(this.touchCurrent)
    if (keys.length == 2) {
      this.dragStart = undefined
      this.dragOffset = undefined

      const current = new Point(e.screen)
      // this.touchCurrent[e.data.identifier] = current

      const cx2 = 0
      const cy2 = 0
      // ищем вторую точку
      for (const key of keys) {
        const k = +key
        // if (k !== e.data.identifier) {
        //   cx2 = this.touchCurrent[k].x
        //   cy2 = this.touchCurrent[k].y
        // }
      }

      // touch len
      const tl = Math.sqrt(Math.pow(current.x - cx2, 2) + Math.pow(current.y - cy2, 2))
      if (this.touchLength < 0) {
        this.touchLength = tl
      }
      // на сколько изменилась длина между касаниями
      const dt = tl - this.touchLength
      this.touchLength = tl

      if (this.scale < 1) {
        this.scale += dt * 0.007 * this.scale
      } else {
        this.scale += dt * 0.01
      }
      this.updateScale()
    } else {
      if (this.dragStart !== undefined && this.dragOffset !== undefined) {
        const p = new Point(e.screen).round()

        p.dec(this.dragStart)

        if (Math.abs(p.x) > 10 || Math.abs(p.y) > 10 || this.dragMoved) {
          this.dragMoved = true
          this.offset.set(this.dragOffset).inc(p)

          this.updateMapScalePos()
        }
      } else {
        // обновляем текущие экранные координаты
        this.lastMoveCoord = new Point(e.screen).round()
      }
    }
  }

  toggleInventory() {
    console.log('openInventory')
    const selectedCharacterId = this.gameData.selectedCharacterId
    if (selectedCharacterId != 0) {
      // если еще нет открытого инвентаря игрока
      if (this.store.getInventoryById(selectedCharacterId) == undefined) {
        this.client.send(ClientPacket.OPEN_MY_INVENTORY)
      } else {
        this.client.send(ClientPacket.INVENTORY_CLOSE, {
          iid: selectedCharacterId
        })
      }
    }
  }

  private updateScale() {
    if (this.scale < 0.05) this.scale = 0.05
    this.updateMapScalePos()
  }

  private onResize() {
    const w = this.app.screen.width
    const h = this.app.screen.height
    // this.app.renderer.resize(w, h)

    this.screenContainer.width = w
    this.screenContainer.height = h

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
        e.preventDefault()
        this.toggleInventory()
        break
      case 'c':
        e.preventDefault()
        this.store.craft.isOpened = !this.store.craft.isOpened
        break
      case 'Enter':
        document.getElementById('inputChat')?.focus()
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