import * as PIXI from 'pixi.js'
import { utils } from '@pixi/core'

export default class Render {

  private readonly canvas: HTMLCanvasElement

  /**
   * PIXI application
   */
  private readonly app: PIXI.Application

  public constructor() {
    console.info('pixi start')

    // создаем канвас для рендера
    this.canvas = document.createElement('canvas')
    this.canvas.id = 'game'
    // но не показываем его, покажем после загрузки всех ресурсов
    this.canvas.style.display = 'none'
    // добавим его в дом дерево
    document.body.appendChild(this.canvas)

    // this.initCanvasHandlers()

    // сглаживание пикселей при масштабировании
    // PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST
    PIXI.TexturePool.textureOptions.scaleMode = 'nearest'

    // проверка мобильной версии
    PIXI.isMobile.any

    this.app = new PIXI.Application()
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
    PIXI.Assets.unload(['base', 'tiles'])

    this.app.destroy({
      removeView: false
    }, {
      children: true
    })

    document.body.removeChild(this.canvas)
  }

  setup() {
    this.canvas.style.display = 'block'

    // debug
    const s = PIXI.Sprite.from('clock14')
    this.app.stage.addChild(s)
  }

  private update(_ticker: PIXI.Ticker): void {
    // console.log('update', ticker)
    // console.log(this.app)
  }
}