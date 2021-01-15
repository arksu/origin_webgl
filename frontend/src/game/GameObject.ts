import MoveController from "@/game/MoveController";
import {ObjectAdd} from "@/net/Packets";
import ObjectView from "@/game/ObjectView";

export interface GameObject extends ObjectAdd {
    moveController?: MoveController
    view?: ObjectView
}
