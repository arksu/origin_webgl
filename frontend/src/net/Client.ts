import {GameObject} from "@/game/GameObject";
import {MapData} from "@/game/Grid";
import store from "@/store/store";
import {MutationTypes} from "@/store/mutation-types";


export default class Client {

    public static instance: Client;

    /**
     * url для открытия websocket коннекта
     */
    public static wsUrl: string;

    /**
     * url для работы с API методами бекенда (авторизация, регистрация и тд)
     */
    public static apiUrl: string;


    /**
     * была ли хоть одна попытка входа?
     */
    public wasLoginTry: boolean = false;

    /**
     * надо ли сделать автоматическую попытку входа при загрузке логин экрана
     */
    public needAutologin: boolean = false;

    /**
     * данные карты (тайлы)
     */
    public map: { [key: string]: MapData } = {};

    /**
     * игровые объекты полученные с сервера
     */
    public objects: { [key: number]: GameObject } = {}

    /**
     * статус моего персонажа
     */
    public myStatus: number[] = []

    public chatHistory: string[] = []
    public onChatMessage ?: Callback

    /**
     * текущие координаты мыши (нужно для позиционирования "руки" при ее создании)
     */
    public mouseX: number = 0
    public mouseY: number = 0

    public get playerObject(): GameObject {
        return this.objects[this.selectedCharacterId]
    }

    /**
     * ид выбранного персонажа
     */
    public get selectedCharacterId(): number {
        return parseInt(localStorage.getItem("selectedCharacterId") || "0");
    }

    public set selectedCharacterId(v: number) {
        localStorage.setItem("selectedCharacterId", v.toString())
    }

    /**
     * очистить игровые данные
     */
    public clear() {
        this.map = {};
        this.objects = {}
        this.chatHistory = []
        store.commit(MutationTypes.INVENTORIES_CLEAR)
    }
}