import {GameObject} from "@/game/GameObject";

export const MAP_DATA = "m";
export const MAP_CONFIRMED = "mc";
export const OBJECT_ADD = "oa";
export const OBJECT_DELETE = "od";
export const OBJECT_MOVE = "om";
export const OBJECT_STOP = "os";
export const STATUS_UPDATE = "su";
export const CONTEXT_MENU = "cm";
export const ACTION_PROGRESS = "ap";
export const INVENTORY_UPDATE = "iv";
export const INVENTORY_CLOSE = "ic";
export const PLAYER_HAND = "ph";
export const CREATURE_SAY = "cs";
export const FILE_CHANGED = "fc";

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
    readonly icon ?: string
    // offset in px
    readonly mx : number
    readonly my : number
}
