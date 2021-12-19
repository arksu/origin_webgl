import {defineStore} from "pinia";

export const useGameStore = defineStore('game', {
    state: () => ({
        selectedCharacterId: undefined as number | undefined
    }),
    actions: {
        clear() {
            this.selectedCharacterId = undefined
        }
    }
})
