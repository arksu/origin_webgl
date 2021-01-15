export interface MapGridData {
    readonly x: number
    readonly y: number
    readonly a: number
    readonly tiles: number[]
}

export interface ObjectAdd {
    id: number
    x: number
    y: number
    heading: number
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
