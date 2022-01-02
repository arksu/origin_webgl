<template>
  <div class="container">
    <img alt="sky" src="../../../assets/img/daysky.png">
    <img alt="sky" :style="{opacity: store.time.nv / 255}" src="../../../assets/img/nightsky.png">

    <img alt="sky" src="../../../assets/img/dayscape.png">
    <img alt="sky" :style="{opacity: store.time.nv / 255}" src="../../../assets/img/nightscape.png">
    <div class="time">
      <span>{{ pad(store.time.h, 2) }}:{{ pad(store.time.m, 2) }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

export default defineComponent({
  name: "DayTime",
  setup() {
    const store = useGameStore()

    return {store}
  }
})
</script>

<style scoped lang="scss">
.container {
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