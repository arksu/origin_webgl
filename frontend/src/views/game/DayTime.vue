<script lang="ts">
import {defineComponent, ref} from 'vue'
import { pad } from '@/util/util'
import { useGameStore } from '@/stores/gameStore'

export default defineComponent({
  name: "DayTime",
  methods: {
    pad
  },
  setup() {
    const store = useGameStore()

    // offset center 60, 26
    const sunL = ref(23)

    return {store, sunL}
  }
})
</script>

<template>
  <div class="window-container" v-if="store.time !== undefined">
    <img alt="sky" src="../../../assets/img/daysky.png">
    <img alt="sky" :style="{opacity: store.time.nv / 255}" src="../../../assets/img/nightsky.png">

    <img alt="sun" :style="{'left' : 60+(store.sunX * sunL)+'px', 'top' : 26+(store.sunY * sunL)+'px'}" src="../../../assets/img/sun.png">

    <img alt="scape" src="../../../assets/img/dayscape.png">
    <img alt="scape" :style="{opacity: store.time.nv / 255}" src="../../../assets/img/nightscape.png">

    <div class="time">
      <span>{{ pad(store.time.h, 2) }}:{{ pad(store.time.m, 2) }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.window-container {
  position: absolute;
  top: 20px;
  left: 50%;
  margin-left: -72px;
  margin-top: -1px;
  width: 134px;
  height: 36px;
}

img {
  position: absolute;
  pointer-events: auto;
}

.time {
  position: center;
  text-align: center;
  left: 50%;
  margin-top: 66px;
  color: #3db67f;
  text-shadow: 1px 0 1px #000,
  0 1px 1px #000,
  -1px 0 1px #000,
  0 -1px 1px #000;
  font-size: 12px;
}
</style>