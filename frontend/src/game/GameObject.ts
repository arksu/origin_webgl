import type { ObjectAdd } from '@/net/packets'
import type ObjectView from '@/game/ObjectView'
import type MoveController from '@/game/MoveController'

export default interface GameObject extends ObjectAdd {
  view?: ObjectView
  moveController?: MoveController
}