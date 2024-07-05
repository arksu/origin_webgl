export enum ServerPacket {
  MAP_DATA = 'm',
  MAP_CONFIRMED = 'mc',
  OBJECT_ADD = 'oa',
  OBJECT_DELETE = 'od',
  OBJECT_MOVE = 'om',
  OBJECT_STOP = 'os',
  STATUS_UPDATE = 'su',
  CONTEXT_MENU = 'cm',
  ACTION_PROGRESS = 'ap',
  INVENTORY_UPDATE = 'iv',
  INVENTORY_CLOSE = 'ic',
  PLAYER_HAND = 'ph',
  CREATURE_SAY = 'cs',
  TIME_UPDATE = 'tu',
  CRAFT_LIST = 'cl',
  FILE_CHANGED = 'fc',
}

export interface MapGridData {
  readonly x: number
  readonly y: number

  /**
   * flag: 0-delete 1-add 2-change
   */
  readonly a: number
  readonly tiles: number[]
}
