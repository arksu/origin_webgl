<template>
  <div class="stats-container">
    <!-- hp-->
    <stat-bar
        frame-color="#9e3f41ba"
        bar-back-color="#1c0808"
        bar1-color="#611813"
        bar2-color="#b3261d"
        :percent1="store.hardHp"
        :percent2="store.softHp"
        :tooltip="'HP: ' + store.CUR_SHP + '/' + store.CUR_HHP" />

    <!-- stamina-->
    <stat-bar
        frame-color="#4B81BEBA"
        bar-back-color="#08111c"
        bar1-color="#2243EE"
        :percent1="store.stamina"
        :tooltip="'Stamina: ' + store.CUR_STAMINA"/>

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
  left: 100px;
  top: 10px;
  height: 100px;
}
</style>
