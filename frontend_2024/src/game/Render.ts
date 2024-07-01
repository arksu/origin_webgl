import * as PIXI from 'pixi.js'

export default class Render {

  private readonly canvas: HTMLCanvasElement

  /**
   * PIXI application
   */
  private readonly app: PIXI.Application

  public constructor() {
    console.warn('pixi start')

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

    this.app = new PIXI.Application()
    this.app.init({
      width: window.innerWidth,
      height: window.innerHeight,
      canvas: this.canvas,
      autoDensity: true,
      preserveDrawingBuffer: true,
      powerPreference: 'high-performance',
      antialias: false,
      backgroundColor: 0x333333
    })

  }

  /**
   * загрузить необходимые для старта атласы
   * @private
   */
  private load(): Promise<undefined> {
    return new Promise(function(resolve, reject) {
    })
  }

  stop() {

  }
}