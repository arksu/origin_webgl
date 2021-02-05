import {GameObject} from "@/game/GameObject";

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
