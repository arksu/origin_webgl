import * as PIXI from 'pixi.js';

export function getKeyFlags(e: PIXI.InteractionEvent): number {
    let flags = 0
    if (e.data.originalEvent !== undefined) {
        if (e.data.originalEvent.shiftKey) flags += 1
        if (e.data.originalEvent.altKey) flags += 2
        if (e.data.originalEvent.ctrlKey) flags += 4
        if (e.data.originalEvent.metaKey) flags += 8
    }
    return flags
}
