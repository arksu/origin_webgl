import type GameResponseDTO from '@/net/GameResponseDTO'
import type GameClient from '@/net/GameClient'

/**
 * реализация игрового протокола
 */
export default class GameProto {

  private client: GameClient

  constructor(client: GameClient) {
    this.client = client
  }

  processMessage(r: GameResponseDTO) {

  }
}