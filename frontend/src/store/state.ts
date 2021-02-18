import {InventoryUpdate} from "@/net/Packets";

export const state = {
    ssid: localStorage.getItem("ssid") || undefined as undefined | string,
    inventories: [] as InventoryUpdate[]
}

export type State = typeof state