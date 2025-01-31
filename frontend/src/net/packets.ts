export enum ClientPacket {
  OBJECT_CLICK = 'oc',
  OBJECT_RIGHT_CLICK = 'orc',
  MAP_CLICK = 'mc',
  KEY_DOWN = 'kd',
  CHAT = 'chat',
  OPEN_MY_INVENTORY = 'openmyinv',
  INVENTORY_CLOSE = 'invclose',
  ITEM_CLICK = 'itemclick',
  ITEM_RIGHT_CLICK = 'itemrclick',
  CONTEXT_MENU_SELECT = 'cmselect',
  CRAFT = 'craft',
  ACTION = 'action',
}

export enum ServerPacket {
  MAP_DATA = 'm',
  MAP_CONFIRMED = 'mc',
  OBJECT_ADD = 'oa',
  OBJECT_DELETE = 'od',
  OBJECT_MOVE = 'om',
  OBJECT_STOP = 'os',
  OBJECT_LIFT = 'ol',
  STATUS_UPDATE = 'su',
  CONTEXT_MENU = 'cm',
  ACTION_PROGRESS = 'ap',
  INVENTORY_UPDATE = 'iv',
  INVENTORY_CLOSE = 'ic',
  PLAYER_HAND = 'ph',
  CREATURE_SAY = 'cs',
  TIME_UPDATE = 'tu',
  CRAFT_LIST = 'cl',
  CURSOR = 'cu',
  FILE_CHANGED = 'fc',
}

export interface AuthorizeTokenResponse {
  readonly characterId: number
  readonly proto: string
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

export interface ObjectAdd {
  id: number
  x: number
  y: number

  /**
   * heading
   */
  h: number

  /**
   * class
   */
  c: string

  /**
   * type id
   */
  t: number

  /**
   * resource path
   */
  r: string

  // a?: PcAppearance
}

export interface ObjectDel {
  readonly id: number
}

export interface ObjectMoved {
  readonly id: number
  readonly x: number
  readonly y: number
  readonly tx: number
  readonly ty: number
  readonly s: number
  readonly mt: string
}

export interface ObjectStopped {
  readonly id: number
  readonly x: number
  readonly y: number
}

export interface ObjectLift {
  /**
   * 0 | 1 - down | up
   */
  readonly l : number

  /**
   * owner id
   */
  readonly oid : number

  /**
   * object id
   */
  readonly id : number 

  // все что ниже - копися с object add - нужно когда объект ставим на землю. фактически это спавн
  x: number
  y: number

  /**
   * heading
   */
  h: number

  /**
   * class
   */
  c: string

  /**
   * type id
   */
  t: number

  /**
   * resource path
   */
  r: string

  // a?: PcAppearance
}

export interface ContextMenuData {
  // ид объекта
  readonly id: number
  // список действий
  readonly l: string[]
}

export interface ActionProgressData {
  /**
   * current
   */
  readonly c: number

  /**
   * total
   */
  readonly t: number
}

export interface StatusUpdateAttribute {
  // type
  readonly t: number
  // value
  readonly v: number
}

export interface StatusUpdate {
  readonly id: number
  readonly l: StatusUpdateAttribute[]
}

export interface CreatureSay {
  readonly id: number
  readonly c: number
  readonly t: string
  readonly ti: string
}

export interface InvItem {
  readonly id: number
  readonly x: number
  readonly y: number
  readonly w: number
  readonly h: number
  readonly q: number
  readonly icon: string
  readonly c: string
}

export interface InventoryUpdate {
  readonly id: number
  readonly t: string
  readonly w: number
  readonly h: number
  readonly l: InvItem[]
}

export interface HandData {
  readonly icon?: string
  // offset in px
  readonly mx: number
  readonly my: number
}

export interface TimeUpdate {
  // world ticks
  readonly t: number
  // in game hour
  readonly h: number
  // minute
  readonly m: number
  // day
  readonly d: number,
  // month
  readonly  mm: number,
  // night value 0-255
  readonly nv: number,
  // sun value 0-255
  readonly sv: number,
  // moon value
  readonly mv: number,
}

export interface CraftItemData {
  readonly icon: string
  readonly count: number
}

export interface CraftData {
  readonly name: string
  readonly produced: CraftItemData[]
  readonly required: CraftItemData[]
}

export interface CraftList {
  readonly list: CraftData[]
}

export interface Cursor {
  readonly c: string
}