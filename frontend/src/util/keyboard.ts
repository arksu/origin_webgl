import * as PIXI from 'pixi.js'

export function getKeyFlags(e: PIXI.FederatedPointerEvent): number {
  let flags = 0
  if (e.shiftKey) flags += 1
  if (e.altKey) flags += 2
  if (e.ctrlKey) flags += 4
  if (e.metaKey) flags += 8
  return flags
}