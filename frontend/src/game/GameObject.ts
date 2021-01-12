import MoveController from "@/game/MoveController";

export interface GameObject {
    id: number
    x: number
    y: number
    moveController?: MoveController
}