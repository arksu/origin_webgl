import * as PIXI from 'pixi.js';

export default class Point {
    public x: number;
    public y: number;

    constructor(x: number, y: number);
    constructor(p: PIXI.Point);
    constructor(p: Point);

    constructor(x: number | PIXI.Point | Point, y?: number) {
        if (x instanceof PIXI.Point) {
            this.x = x.x;
            this.y = x.y;
        } else if (x instanceof Point) {
            this.x = x.x;
            this.y = x.y;
        } else {
            this.x = x;
            this.y = y!!;
        }
    }

    public dec(p: Point): Point {
        this.x -= p.x;
        this.y -= p.y;
        return this;
    }

    public inc(p: Point): Point {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    public plus(p: Point): Point {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    public set(p: Point): Point {
        this.x = p.x;
        this.y = p.y;
        return this;
    }

    public round(): Point {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        return this;
    }

    public toString(): string {
        return this.x + " " + this.y;
    }
}