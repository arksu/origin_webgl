import MoveController from "@/game/MoveController";
import {ObjectAdd} from "@/net/Packets";

export interface GameObject extends ObjectAdd {
    moveController?: MoveController
}
