import type GameResponseDTO from '@/net/GameResponseDTO'
import type GameClient from '@/net/GameClient'
import {
  type ActionProgressData,
  type ContextMenuData,
  type CreatureSay,
  type HandData,
  type InventoryUpdate,
  type MapGridData,
  type ObjectAdd,
  type ObjectDel,
  type ObjectMoved,
  type ObjectStopped,
  ServerPacket
} from '@/net/packets'
import Render from '@/game/Render'
import type GameData from '@/net/GameData'
import MoveController from '@/game/MoveController'
import { type ChatItem, useGameStore } from '@/stores/gameStore'
import { mouse } from '@/game/Mouse'

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
    const store = useGameStore()
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
          gameData.objects[pkt.id].moveController = old.moveController
          gameData.objects[pkt.id].view = old.view
        }
        this.render.onObjectAdd(gameData.objects[data.id])
        this.render.updateMapScalePos()
        break
      }

      case ServerPacket.OBJECT_DELETE : {
        const pkt = <ObjectDel>data
        const obj = gameData.objects[pkt.id]
        obj?.moveController?.stop()
        this.render.onObjectDelete(obj)
        delete gameData.objects[pkt.id]
        break
      }

      case ServerPacket.OBJECT_MOVE : {
        const pkt = <ObjectMoved>data
        const obj = gameData.objects[pkt.id]
        if (obj !== undefined) {
          if (obj.moveController === undefined) {
            obj.moveController = new MoveController(this.render, obj, pkt)
          } else {
            obj.moveController.applyData(pkt)
          }
        }
        break
      }

      case ServerPacket.OBJECT_STOP : {
        const pkt = <ObjectStopped>data
        const obj = gameData.objects[pkt.id]
        if (obj !== undefined) {
          if (obj.moveController !== undefined) {
            obj.moveController.serverStop(pkt)
          } else {
            obj.x = data.x
            obj.y = data.y
            this.render.onObjectMoved(obj)
          }
        }

        if (this.gameData.selectedCharacterId == pkt.id) {
          this.render.updateMapScalePos()
        }
        break
      }

      case ServerPacket.CONTEXT_MENU : {
        const pkt = <ContextMenuData>data
        const obj = gameData.objects[pkt.id]
        if (obj !== undefined) {
          // const sc = this.render.coordGame2ScreenAbs(obj.x, obj.y)
          store.contextMenuPosX = mouse.x
          store.contextMenuPosY = mouse.y
          store.contextMenu = pkt
        } else {
          store.contextMenu = undefined
        }
        break
      }

      case ServerPacket.CREATURE_SAY : {
        const pkt = <CreatureSay>data
        const item: ChatItem = {
          title: pkt.ti,
          text: pkt.t,
          channel: pkt.c
        }

        // сохраним сообщение в сторе
        const len = store.chatLines.push(item)
        // ограничим максимальную длину истории чата
        const MAX_LEN = 10
        if (len > MAX_LEN) {
          store.chatLines.splice(0, len - MAX_LEN)
        }
        break
      }

      case ServerPacket.INVENTORY_UPDATE : {
        const pkt = <InventoryUpdate>data
        store.setInventory(pkt)
        break
      }

      case ServerPacket.INVENTORY_CLOSE : {
        store.closeInventory(data.id)
        break
      }

      case ServerPacket.PLAYER_HAND : {
        const pkt = <HandData>data
        store.hand = pkt.icon !== undefined ? pkt : undefined
        break
      }

      case ServerPacket.ACTION_PROGRESS : {
        const pkt = <ActionProgressData>data
        store.actionProgress.total = pkt.t
        store.actionProgress.current = pkt.c
        break
      }
    }
  }
}