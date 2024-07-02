import type GameResponseDTO from '@/net/GameResponseDTO'
import type GameClient from '@/net/GameClient'
import { type MapGridData, ServerPacket } from '@/net/packets'
import type GameObject from '@/game/GameObject'
import type { MapData } from '@/game/Grid'
import Render from '@/game/Render'

/**
 * реализация игрового протокола
 */
export default class GameProto {

  private readonly client: GameClient
  private readonly render: Render

  /**
   * данные карты (тайлы)
   */
  public map: { [key: string]: MapData } = {}

  /**
   * игровые объекты полученные с сервера
   */
  public objects: { [key: number]: GameObject } = {}

  /**
   * ид выбранного персонажа
   */
  public selectedCharacterId: number = 0

  get playerObject(): GameObject {
    return this.objects[this.selectedCharacterId]
  }

  constructor(client: GameClient, render: Render) {
    this.client = client
    this.render = render
  }

  processMessage(r: GameResponseDTO) {
    const channel = r.c
    const data = r.d
    switch (channel) {
      case ServerPacket.MAP_DATA: {
        const p = (<MapGridData>data)
        const key = p.x + '_' + p.y
        console.log('map', key)
        switch (p.a) {
          case 0 : // delete
            delete this.map[key]
            this.render.deleteGrid(p.x, p.y)
            break
          case 1: // add
            this.map[key] = {
              x: p.x,
              y: p.y,
              tiles: p.tiles,
              isChanged: false
            }
            // после добалвения всех гридов придет пакет MAP_CONFIRMED
            break
          case 2: // change
            this.map[key].tiles = p.tiles
            this.map[key].isChanged = true
            this.render.addGrid(p.x, p.y)
            break
        }
        break
      }
      case ServerPacket.MAP_CONFIRMED : {
        for (const mapKey in this.map) {
          const s = mapKey.split('_')
          const x: number = +s[0]
          const y: number = +s[1]
          this.render.addGrid(x, y)
        }
        break
      }
    }
  }
}