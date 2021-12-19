import {defineStore} from "pinia";

export const useGameStore = defineStore('game', {
    state: () => ({
        selectedCharacterId: undefined as number | undefined
    }),
    actions: {

    }
})
