import type { ObjectAdd } from '@/net/packets'
import type ObjectView from '@/game/ObjectView'

export default interface GameObject extends ObjectAdd {
  view?: ObjectView
}