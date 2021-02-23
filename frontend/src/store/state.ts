import {HandData, InventoryUpdate} from "@/net/Packets";

export const state = {
    /**
     * ид текущей сессии используемый для запросов серверу
     */
    ssid: localStorage.getItem("ssid") || undefined as undefined | string,

    /**
     * последняя ошибка зарегистрированная
     */
    lastError: undefined as string | undefined,

    /**
     * список инвентарей открытых в данный момент которые прислал нам сервер
     */
    inventories: [] as InventoryUpdate[],

    /**
     * то что держим в руке
     */
    hand: undefined as undefined | HandData
}

export type State = typeof state