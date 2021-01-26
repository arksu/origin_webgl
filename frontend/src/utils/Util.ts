export interface Coord extends Array<number | string> {
    0: number;
    1: number;
    length: 2; // это литеральный тип '2', это не значение!
}

export interface Layer {
    img: string
    interactive?: boolean
    offset?: Coord
    z?: number
    shadow?: boolean
}

export interface Resource {
    size?: Coord
    offset?: Coord
    layers: Layer[]
}

export function hexToBase64(hexStr: string): string {
    const hexStrLen = hexStr.length;
    let hexAsciiCharsStr = '';
    for (let i = 0; i < hexStrLen; i += 2) {
        const hexPair = hexStr.substring(i, i + 2);
        const hexVal = parseInt(hexPair, 16);
        hexAsciiCharsStr += String.fromCharCode(hexVal);
    }
    return btoa(hexAsciiCharsStr);
}

export function log2(n: number) {
    let log = 0;
    if ((n & 0xffff0000) !== 0) {
        n >>>= 16;
        log = 16;
    }
    if (n >= 256) {
        n >>>= 8;
        log += 8;
    }
    if (n >= 16) {
        n >>>= 4;
        log += 4;
    }
    if (n >= 4) {
        n >>>= 2;
        log += 2;
    }
    log = log + (n >>> 1);
    return log;
}

/**
 * получить случайное число в диапазоне (0..max-1)
 */
export function getRandomInt(max: number): number {
    return Math.floor(Math.random() * Math.floor(max));
}

export function getRandomByCoord(x: number, y: number): number {
    // TODO детерменированный рандом на основании координат
    return getRandomInt(10000);
}

/**
 * ориентация экран сейчас планшетная? (горизонтальная)
 */
export function isOrientationLandscape(): boolean {
    if (window.screen.orientation !== undefined) {
        const type = window.screen.orientation.type;
        return type !== "portrait-primary" && type !== "portrait-secondary";
    }
    const mql = window.matchMedia("(orientation: portrait)");
    return !mql.matches;
}