import type GameResponseDTO from '@/net/GameResponseDTO'
import type GameClient from '@/net/GameClient'
import { type MapGridData, type ObjectAdd, type ObjectDel, ServerPacket } from '@/net/packets'
import Render from '@/game/Render'
import type GameData from '@/net/GameData'

/**
 * реализация игрового протокола
 */
export default class GameProto {

  private readonly client: GameClient
  private readonly render: Render
  private readonly gameData: GameData

  constructor(client: GameClient, render: Render) {
    this.client = client
    this.render = render
    this.gameData = render.gameData
  }

  processMessage(r: GameResponseDTO) {
    const gameData = this.gameData
    const channel = r.c
    const data = r.d
    switch (channel) {

      case ServerPacket.MAP_DATA: {
        const pkt = <MapGridData>data
        const key = pkt.x + '_' + pkt.y
        console.log('map', key)
        switch (pkt.a) {
          case 0 : // delete
            delete gameData.map[key]
            this.render.deleteGrid(pkt.x, pkt.y)
            break
          case 1: // add
            gameData.map[key] = {
              x: pkt.x,
              y: pkt.y,
              tiles: pkt.tiles,
              isChanged: false
            }
            // после добалвения всех гридов придет пакет MAP_CONFIRMED
            break
          case 2: // change
            gameData.map[key].tiles = pkt.tiles
            gameData.map[key].isChanged = true
            this.render.addGrid(pkt.x, pkt.y)
            break
        }
        break
      }

      case ServerPacket.MAP_CONFIRMED : {
        for (const mapKey in gameData.map) {
          const s = mapKey.split('_')
          const x: number = +s[0]
          const y: number = +s[1]
          this.render.addGrid(x, y)
        }
        break
      }

      case ServerPacket.OBJECT_ADD : {
        const pkt = <ObjectAdd>data
        const old = gameData.objects[pkt.id]
        gameData.objects[pkt.id] = pkt
        if (old !== undefined) {
          // gameData.objects[pkt.id].moveController = old.moveController
          // gameData.objects[pkt.id].view = old.view
        }
        this.render.onObjectAdd(gameData.objects[data.id])
        this.render.updateMapScalePos()
        break
      }

      case ServerPacket.OBJECT_DELETE : {
        const obj = gameData.objects[(<ObjectDel>data).id];
        // obj?.moveController?.stop()
        this.render.onObjectDelete(obj)
        delete gameData.objects[(<ObjectDel>data).id];
        break;
      }
    }
  }
}