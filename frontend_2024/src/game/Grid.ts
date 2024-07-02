import * as PIXI from 'pixi.js'
import Tile from './Tile'
import GameClient from '@/net/GameClient'
import VertexBuffer from '@/util/VertexBuffer'
import { getRandomByCoord } from '@/util/random'

export interface MapData {
  x: number
  y: number

  /**
   * массив тайлов
   */
  tiles: number[]

  /**
   * было ли изменение тайлов
   */
  isChanged: boolean
}


export default class Grid {
  // 2 4 5 10
  private static readonly DIVIDER = 4

  private static readonly MAKE_CORNERS = true

  private static readonly PROGRAM: PIXI.GlProgram = PIXI.GlProgram.from({
    vertex:
      `precision mediump float;

    attribute vec2 aVertexPosition;
    attribute vec2 aTextureCoord;

    uniform mat3 translationMatrix;
    uniform mat3 projectionMatrix;

    varying vec2 vUvs;

    void main() {

        vUvs = aTextureCoord;
        gl_Position = vec4((projectionMatrix * translationMatrix * vec3(aVertexPosition, 1.0)).xy, 0.0, 1.0);

    }`,
    fragment:
      `precision mediump float;

    varying vec2 vUvs;

    uniform sampler2D uSamplerTexture;

    void main() {

        gl_FragColor = texture2D(uSamplerTexture, vUvs);
    }`
  })

  /**
   * размер одной стороны чанка
   */
  private readonly CHUNK_SIZE = Tile.GRID_SIZE / Grid.DIVIDER

  private static readonly bx = [0, 1, 2, 1]
  private static readonly by = [1, 0, 1, 2]
  private static readonly cx = [0, 0, 2, 2]
  private static readonly cy = [0, 2, 2, 0]

  /**
   * чанки-контейнеры на которые делим грид (иначе по размеру текстуры целый грид не влезает в память)
   */
  public chunks: PIXI.Container[] = []

  private parent: PIXI.Container

  /**
   * координаты грида (в координатах гридов)
   */
  public readonly x: number
  public readonly y: number
  /**
   * абсолютные игровые координаты
   */
  public readonly absoluteX: number
  public readonly absoluteY: number

  public readonly key: string

  private _visible: boolean = true

  constructor(parent: PIXI.Container, x: number, y: number) {
    this.parent = parent
    this.x = x
    this.y = y
    this.absoluteX = x * Tile.FULL_GRID_SIZE + Tile.FULL_GRID_SIZE / 2
    this.absoluteY = y * Tile.FULL_GRID_SIZE + Tile.FULL_GRID_SIZE / 2
    this.key = this.x + '_' + this.y

    // замерим время на создание грида
    const timerName = 'make grid ' + this.key
    console.time(timerName)

    this.makeChunks()

    console.timeEnd(timerName)
  }

  private makeChunks() {
    // создаем чанки грида 4x4
    for (let cx = 0; cx < Grid.DIVIDER; cx++) {
      for (let cy = 0; cy < Grid.DIVIDER; cy++) {
        const container = this.makeChunk(cx, cy)
        this.chunks.push(container)
        this.parent.addChild(container)
      }
    }
  }

  public destroy() {
    for (const container of this.chunks) {
      container.destroy({
        children: true
      })
    }
    this.chunks = []
  }

  /**
   * управление видимостью грида (скрываем для кэширования)
   */
  public set visible(v: boolean) {
    if (this._visible != v) {
      this._visible = v
      for (let i = 0; i < this.chunks.length; i++) {
        this.chunks[i].visible = v
      }
    }
  }

  public get visible() {
    return this._visible
  }

  /**
   * перестроить грид (данные тайлов изменились)
   */
  public rebuild() {
    console.log('grid rebuild', this.key)
    this.destroy()
    this.makeChunks()
  }

  private makeChunk(cx: number, cy: number): PIXI.Container {
    const container = new PIXI.Container()
    container.sortableChildren = true
    // координаты грида с учетом чанка
    const x = this.x + cx / Grid.DIVIDER
    const y = this.y + cy / Grid.DIVIDER
    // координаты грида ставим в абсолютные мировые в тайлах
    container.x = x * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - y * Tile.TILE_WIDTH_HALF * Tile.GRID_SIZE - Tile.TILE_WIDTH_HALF
    container.y = x * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE + y * Tile.TILE_HEIGHT_HALF * Tile.GRID_SIZE

    this.makeTiles(container, cx, cy)
    container.calculateBounds()
    // console.log("grid screen x=" + container.x + " y=" + container.y + " w=" + container.width + " h=" + container.height)

    return container
  }

  private makeTiles(container: PIXI.Container, cx: number, cy: number) {
    const gameData = GameClient.data
    const tiles = gameData.map[this.key].tiles

    // создаем заранее массив в 2 раза больше чем надо (под кусочки тайлов)
    const elements = this.CHUNK_SIZE * this.CHUNK_SIZE * 2

    const vertexBuffer = new VertexBuffer(elements)

    let tr: number[][] = []

    for (let tx = 0; tx < this.CHUNK_SIZE; tx++) {
      for (let ty = 0; ty < this.CHUNK_SIZE; ty++) {

        const x = cx * this.CHUNK_SIZE + tx
        const y = cy * this.CHUNK_SIZE + ty
        // индекс в массиве тайлов
        const idx = y * Tile.GRID_SIZE + x

        const tn = Tile.getGroundTexture(tiles[idx], x, y)
        if (tn !== undefined) {
          const path = tn

          const sx = tx * Tile.TILE_WIDTH_HALF - ty * Tile.TILE_WIDTH_HALF
          const sy = tx * Tile.TILE_HEIGHT_HALF + ty * Tile.TILE_HEIGHT_HALF

          vertexBuffer.addVertex(sx, sy, Tile.TEXTURE_WIDTH, Tile.TEXTURE_HEIGHT, PIXI.Texture.from(path))

//=================================================================

          let wasCorners = false
          if (Grid.MAKE_CORNERS) {
            tr = []
            // идем по тайлам вокруг целевого и заполним массив окружающих тайлов tr
            for (let rx = -1; rx <= 1; rx++) {
              tr[rx + 1] = []
              for (let ry = -1; ry <= 1; ry++) {
                if (rx == 0 && ry == 0) {
                  tr[rx + 1][ry + 1] = 0
                  continue
                }

                const dx = x + rx
                const dy = y + ry
                let tn = -1
                // это тайл еще текущего грида
                if (dx >= 0 && dx < Tile.GRID_SIZE && dy >= 0 && dy < Tile.GRID_SIZE) {
                  tn = tiles[dy * Tile.GRID_SIZE + dx]
                } else {
                  // тайл соседнего грида
                  // смещение тайла который вылез за границы относительно текущего грида
                  const ox = dx < 0 ? -1 : (dx >= Tile.GRID_SIZE ? 1 : 0)
                  const oy = dy < 0 ? -1 : (dy >= Tile.GRID_SIZE ? 1 : 0)
                  const ndata = gameData.map[(this.x + ox) + '_' + (this.y + oy)]
                  // можем выйти за границы карты и такого грида не будет
                  if (ndata !== undefined) {
                    const ix = dx < 0 ? Tile.GRID_SIZE + dx : (dx >= Tile.GRID_SIZE ? dx - Tile.GRID_SIZE : dx)
                    const iy = dy < 0 ? Tile.GRID_SIZE + dy : (dy >= Tile.GRID_SIZE ? dy - Tile.GRID_SIZE : dy)
                    tn = ndata.tiles[iy * Tile.GRID_SIZE + ix]
                  }
                }
                tr[rx + 1][ry + 1] = tn
              }
            }

            if (tr[0][0] >= tr[1][0]) tr[0][0] = -1
            if (tr[0][0] >= tr[0][1]) tr[0][0] = -1
            if (tr[2][0] >= tr[1][0]) tr[2][0] = -1
            if (tr[2][0] >= tr[2][1]) tr[2][0] = -1
            if (tr[0][2] >= tr[0][1]) tr[0][2] = -1
            if (tr[0][2] >= tr[1][2]) tr[0][2] = -1
            if (tr[2][2] >= tr[2][1]) tr[2][2] = -1
            if (tr[2][2] >= tr[1][2]) tr[2][2] = -1

            // текущий (центральный тайл)
            for (let i = tiles[idx] - 1; i >= 0; i--) {
              const ts = Tile.sets[i]
              if (ts == undefined || ts.corners == undefined || ts.borders == undefined) continue
              let bm = 0
              let cm = 0
              for (let o = 0; o < 4; o++) {
                if (tr[Grid.bx[o]][Grid.by[o]] == i) bm |= 1 << o
                if (tr[Grid.cx[o]][Grid.cy[o]] == i) cm |= 1 << o
              }
              if (bm !== 0) {
                const arr = ts.borders[bm - 1]
                if (arr !== undefined) {
                  const path = arr.get(getRandomByCoord(x, y))
                  if (path !== undefined) {
                    wasCorners = true
                    vertexBuffer.addVertex(sx, sy, Tile.TEXTURE_WIDTH, Tile.TEXTURE_HEIGHT, PIXI.Texture.from(path))
                  }
                }
              }
              if (cm !== 0) {
                const arr = ts.corners[cm - 1]
                if (arr !== undefined) {
                  const path = arr.get(getRandomByCoord(x, y))
                  if (path !== undefined) {
                    wasCorners = true
                    vertexBuffer.addVertex(sx, sy, Tile.TEXTURE_WIDTH, Tile.TEXTURE_HEIGHT, PIXI.Texture.from(path))
                  }
                }
              }
            }
          }
// ==========================================================================
          if (!wasCorners) {
            const terrain = Tile.terrains[tiles[idx]]
            if (terrain !== undefined) {
              const sprList = terrain.generate(x, y, sx + Tile.TILE_WIDTH_HALF, sy + Tile.TILE_HEIGHT_HALF)
              if (sprList !== undefined) {
                for (let i = 0; i < sprList.length; i++) {
                  container.addChild(sprList[i])
                }
              }
            }
          }
        }
      }
    }

    vertexBuffer.finish()
    const geometry = new PIXI.MeshGeometry(vertexBuffer.vertex, vertexBuffer.uv, vertexBuffer.index)

    // const mesh = new PIXI.Mesh(
    //   geometry,
    //   new PIXI.Shader(Grid.PROGRAM, {
    //     uSamplerTexture: PIXI.Texture.from(Tile.ATLAS)
    //   }),
    //   PIXI.State.for2d(),
    //   PIXI.DRAW_MODES.TRIANGLES
    // )
    //
    // container.addChild(mesh)
  }
}
