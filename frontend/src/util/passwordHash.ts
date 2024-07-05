import {syncScrypt} from "scrypt-js";

export function makeHash(str: string): string {

    const enc = new TextEncoder();
    const passwordArray = enc.encode(str);

    // формируем scrypt hash
    const N = 2048, r = 8, p = 1;
    const dkLen = 32;

    // генерим случайную соль каждый раз
    const saltBuffer = new Uint8Array(16);
    window.crypto.getRandomValues(saltBuffer);

    const hexFun = function (x: number) { return ('00' + x.toString(16)).slice(-2);}

    const saltHex: string = Array.prototype.map.call(saltBuffer, hexFun).join('');

    const hashHex = Array.prototype.map.call(syncScrypt(passwordArray, saltBuffer, N, r, p, dkLen), hexFun).join('');

    const params: number = log2(N) << 16 | r << 8 | p;
    const hash = '$s0$' + params.toString(16) + '$' + hexToBase64(saltHex) + '$' + hexToBase64(hashHex);
    console.log("password hash: " + hash)
    return hash;
}

function log2(n: number): number {
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

function hexToBase64(hexStr: string): string {
    const hexStrLen = hexStr.length;
    let hexAsciiCharsStr = '';
    for (let i = 0; i < hexStrLen; i += 2) {
        const hexPair = hexStr.substring(i, i + 2);
        const hexVal = parseInt(hexPair, 16);
        hexAsciiCharsStr += String.fromCharCode(hexVal);
    }
    return window.btoa(hexAsciiCharsStr);
}
