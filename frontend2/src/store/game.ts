import {defineStore} from "pinia";

export const useGameStore = defineStore('game', {
    state: () => ({
        selectedCharacterId: undefined as number | undefined,
        CUR_SHP : 0 as number,
        CUR_HHP : 0 as number,
        MAX_HP : 0 as number,
        CUR_STAMINA : 0 as number,
        MAX_STAMINA : 0 as number,
        CUR_ENERGY : 0 as number,
        MAX_ENERGY : 0 as number,
    }),
    actions: {

    }
})
