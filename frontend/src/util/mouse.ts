import * as PIXI from 'pixi.js'

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/buttons
 */
export function isButtonPrimary(e: PIXI.FederatedPointerEvent) {
  return e.buttons == 0 || e.buttons == 1
}

export function isButtonMiddle(e: PIXI.FederatedPointerEvent) {
  return e.buttons == 4
}