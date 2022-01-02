import {ObjectAdd} from "../net/packets";
import ObjectView from "./ObjectView";
import MoveController from "./MoveController";

export default interface GameObject extends ObjectAdd {
    moveController?: MoveController
    view?: ObjectView
}
