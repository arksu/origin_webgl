import GameObject from "../game/GameObject";
import {MapData} from "../game/Grid";

export default class GameData {
    /**
     * данные карты (тайлы)
     */
    public map: { [key: string]: MapData } = {};

    /**
     * игровые объекты полученные с сервера
     */
    public objects: { [key: number]: GameObject } = {}

    /**
     * ид выбранного персонажа
     */
    public selectedCharacterId: number = 0

    get playerObject(): GameObject {
        return this.objects[this.selectedCharacterId]
    }
}
