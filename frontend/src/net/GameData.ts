import type GameObject from '@/game/GameObject'
import type { MapData } from '@/game/Grid'

export default class GameData {
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

  get playerObject(): GameObject | undefined {
    return this.objects[this.selectedCharacterId]
  }
}
