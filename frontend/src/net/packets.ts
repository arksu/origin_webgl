import GameObject from "../game/GameObject";

export enum ServerPacket {
    MAP_DATA = "m",
    MAP_CONFIRMED = "mc",
    OBJECT_ADD = "oa",
    OBJECT_DELETE = "od",
    OBJECT_MOVE = "om",
    OBJECT_STOP = "os",
    STATUS_UPDATE = "su",
    CONTEXT_MENU = "cm",
    ACTION_PROGRESS = "ap",
    INVENTORY_UPDATE = "iv",
    INVENTORY_CLOSE = "ic",
    PLAYER_HAND = "ph",
    CREATURE_SAY = "cs",
    TIME_UPDATE = "tu",
    FILE_CHANGED = "fc",
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

export interface PcAppearance {
    readonly n: string
    readonly vt: string
    readonly s: number
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

    a?: PcAppearance
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

export interface ContextMenuData {
    readonly id: number
    readonly l: string[]
    obj: GameObject
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
    // index
    readonly i: number
    // value
    readonly v: number
}

export interface StatusUpdate {
    readonly id: number
    readonly list: StatusUpdateAttribute[]
}

export interface CreatureSay {
    readonly id: number
    readonly c: number
    readonly t: string
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
