import {pbkdf2Sync} from "crypto";
import {BinaryLike, checkAndInit, smixSync} from "@/utils/scrypt/utils";

// N = Cpu cost, r = Memory cost, p = parallelization cost
export function scrypt(key: BinaryLike, salt: BinaryLike, N: number, r: number, p: number, dkLen: number, progressCallback?: any) {
    const {
        XY,
        V,
        B32,
        x,
        _X,
        B,
        tickCallback
    } = checkAndInit(key, salt, N, r, p, dkLen, progressCallback)

    for (let i = 0; i < p; i++) {
        smixSync(B, i * 128 * r, r, N, V, XY, _X, B32, x, tickCallback)
    }

    return pbkdf2Sync(key, B, 1, dkLen, 'sha256')
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
