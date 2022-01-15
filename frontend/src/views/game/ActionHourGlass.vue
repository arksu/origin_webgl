<template>
  <div v-if="store.actionProgress.total > 0" class="window-container">
<!--  такой ублюдочный код - потому что надо сделать preload картинок  -->
<!--  v-if тут не подходит. он будет создавать каждый тег каждый кадр и заметно мерцание пока грузится картинка  -->
    <img :style="{display: store.actionFrame===0 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock0.png" alt="action">
    <img :style="{display: store.actionFrame===1 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock1.png" alt="action">
    <img :style="{display: store.actionFrame===2 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock2.png" alt="action">
    <img :style="{display: store.actionFrame===3 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock3.png" alt="action">
    <img :style="{display: store.actionFrame===4 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock4.png" alt="action">
    <img :style="{display: store.actionFrame===5 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock5.png" alt="action">
    <img :style="{display: store.actionFrame===6 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock6.png" alt="action">
    <img :style="{display: store.actionFrame===7 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock7.png" alt="action">
    <img :style="{display: store.actionFrame===8 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock8.png" alt="action">
    <img :style="{display: store.actionFrame===9 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock9.png" alt="action">
    <img :style="{display: store.actionFrame===10 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock10.png" alt="action">
    <img :style="{display: store.actionFrame===11 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock11.png" alt="action">
    <img :style="{display: store.actionFrame===12 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock12.png" alt="action">
    <img :style="{display: store.actionFrame===13 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock13.png" alt="action">
    <img :style="{display: store.actionFrame===14 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock14.png" alt="action">
    <img :style="{display: store.actionFrame===15 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock15.png" alt="action">
    <img :style="{display: store.actionFrame===16 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock16.png" alt="action">
    <img :style="{display: store.actionFrame===17 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock17.png" alt="action">
    <img :style="{display: store.actionFrame===18 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock18.png" alt="action">
    <img :style="{display: store.actionFrame===19 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock19.png" alt="action">
    <img :style="{display: store.actionFrame===20 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock20.png" alt="action">
    <img :style="{display: store.actionFrame===21 ? 'block' : 'none'}" src="../../../assets/img/hourglass/clock21.png" alt="action">

    <div class="frame">
      <div class="bar-back"></div>
      <div class="bar" :style="{width: Math.round(store.actionProgress.current / store.actionProgress.total * 100) + '%'}"></div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import {useGameStore} from "../../store/game";

/**
 * песочные часы - индикатор прогресса текущего действия
 */
export default defineComponent({
  name: "ActionHourGlass",
  setup() {
    const store = useGameStore()
    return {store}
  }
})
</script>
<style scoped lang="scss">
.window-container {
  position: absolute;
  top: 50%;
  left: 50%;
  margin-left: -16px;
  margin-top: -120px;
  width: 32px;
  height: 36px;
  z-index: 100;
}
.window-container img {
  position: absolute;
}
.frame {
  box-sizing: content-box;
  position: absolute;
  height: 5px;
  width: 120px;
  border: 2px solid #132d15;
  border-radius: 10px;
  pointer-events: auto;
  display:inline-block;
  margin-left: -47px;
  margin-top: 39px;
  //margin-right: 20px;
}

.frame .bar-back {
  background-color: #354b39;
  position: absolute;
  width: 100%;
  height: 5px;
  border-radius: 10px;
}

.frame .bar {
  background-color: #3abd45;
  position: absolute;
  height: 5px;
  border-radius: 10px;
}
</style>
