<template>
  <div class="stats-container">
    <stat-bar
        frame-color="#4B81BEBA"
        bar-color="#2243EE"
        bar-back-color="#08111c"
        :percent="store.stamina"
        :tooltip="'Stamina: ' + store.stamina"/>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../../store/game";
import StatBar from "./StatBar.vue";

export default defineComponent({
  name: "Stats",
  components: {StatBar},
  setup() {
    const store = useGameStore()

    return {store}
  }
})
</script>

<style scoped lang="scss">
.stats-container {
  position: absolute;
  left: 110px;
  top: 16px;
}
</style>
