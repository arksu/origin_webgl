import {InventoryUpdate} from "@/net/Packets";

export const state = {
    inventories: [] as InventoryUpdate[]
}

export type State = typeof state