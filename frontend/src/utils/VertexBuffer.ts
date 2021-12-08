import * as PIXI from 'pixi.js';
import Tile from "@/game/Tile";

export default class VertexBuffer {

    private _vertex: Float32Array
    private _uv: Float32Array
    private _index: Uint16Array

    private _size: number

    private _count: number = 0

    private _finished : boolean = false

    constructor(size: number) {
        this._size = size
        this._vertex = new Float32Array(this._size * 8)
        this._uv = new Float32Array(this._size * 8)
        this._index = new Uint16Array(this._size * 6)
    }

    get vertex(): Float32Array {
        return this._vertex
    }

    get uv(): Float32Array {
        return this._uv
    }

    get index(): Uint16Array {
        return this._index
    }

    public addVertex(x: number, y: number, w: number, h: number, t: PIXI.Texture): void {
        if (this._finished) return

        // если место в массиве кончилось - надо добавить еще элементов
        if (this._count >= this._size) {
            this._size += this._size / 2

            const newVertex = new Float32Array(this._size * 8)
            newVertex.set(this._vertex, 0)
            this._vertex = newVertex

            const newUv = new Float32Array(this._size * 8)
            newUv.set(this._uv, 0)
            this._uv = newUv

            const newIndex = new Uint16Array(this._size * 6)
            newIndex.set(this._index, 0)
            this._index = newIndex
        }

        const index = this._count * 8

        this._vertex[index] = x
        this._vertex[index + 1] = y

        this._vertex[index + 2] = x + w
        this._vertex[index + 3] = y

        this._vertex[index + 4] = x + w
        this._vertex[index + 5] = y + h

        this._vertex[index + 6] = x
        this._vertex[index + 7] = y + h

        this._uv[index] = t._uvs.x0
        this._uv[index + 1] = t._uvs.y0
        this._uv[index + 2] = t._uvs.x1
        this._uv[index + 3] = t._uvs.y1
        this._uv[index + 4] = t._uvs.x2
        this._uv[index + 5] = t._uvs.y2
        this._uv[index + 6] = t._uvs.x3
        this._uv[index + 7] = t._uvs.y3

        const ti = index / 2
        const iIndex = this._count * 6
        this._index[iIndex] = ti
        this._index[iIndex + 1] = ti + 3
        this._index[iIndex + 2] = ti + 1
        this._index[iIndex + 3] = ti + 1
        this._index[iIndex + 4] = ti + 3
        this._index[iIndex + 5] = ti + 2

        this._count++
    }

    public finish() {
        if (this._finished) return

        this._vertex = this._vertex.slice(0, this._count * 8)
        this._uv = this._uv.slice(0, this._count * 8)
        this._index = this._index.slice(0, this._count * 6)
    }
}
