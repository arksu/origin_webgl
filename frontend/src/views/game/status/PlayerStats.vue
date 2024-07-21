<script lang="ts">
import { defineComponent } from 'vue'
import StatBar from './StatBar.vue'
import { useGameStore } from '@/stores/gameStore'

export default defineComponent({
  name: 'Stats',
  components: { StatBar },
  setup() {
    const store = useGameStore()

    return { store }
  }
})
</script>

<template>
  <div class="stats-container">
    <!-- hp-->
    <!--    <stat-bar-->
    <!--        frame-color="#9e3f41ba"-->
    <!--        bar-back-color="#1c0808"-->
    <!--        bar1-color="#611813"-->
    <!--        bar2-color="#b3261d"-->
    <!--        :percent1="store.hardHpPercent"-->
    <!--        :percent2="store.softHpPercent"-->
    <!--        :tooltip="'HP: ' + store.CUR_SHP + '/' + store.CUR_HHP" />-->

    <!-- stamina-->
    <stat-bar
      :percent1="store.staminaPercent"
      :tooltip="'Stamina: ' + store.playerStatus.stamina"
      bar-back-color="#08111c"
      bar1-color="#2243EE"
      frame-color="#4B81BEBA" />

  </div>
</template>

<style lang="scss" scoped>
.stats-container {
  position: absolute;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: flex-start;
  left: 100px;
  top: 1px;
  height: 20px;
  padding: 0;
}
</style>
