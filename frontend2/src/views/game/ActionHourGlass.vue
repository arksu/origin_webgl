<template>
  <div v-if="store.actionProgress.total > 0" class="container">
<!--  такой ублюдочный код - потому что надо сделать preload картинок  -->
<!--  v-if тут не подходит. он будет создавать каждый тег каждый кадр и заметно мерцание пока грузится картинка  -->
    <img :style="{display: store.actionFrame===0 ? 'block' : 'none'}" src="../../../assets/img/clock0.png" alt="action">
    <img :style="{display: store.actionFrame===1 ? 'block' : 'none'}" src="../../../assets/img/clock1.png" alt="action">
    <img :style="{display: store.actionFrame===2 ? 'block' : 'none'}" src="../../../assets/img/clock2.png" alt="action">
    <img :style="{display: store.actionFrame===3 ? 'block' : 'none'}" src="../../../assets/img/clock3.png" alt="action">
    <img :style="{display: store.actionFrame===4 ? 'block' : 'none'}" src="../../../assets/img/clock4.png" alt="action">
    <img :style="{display: store.actionFrame===5 ? 'block' : 'none'}" src="../../../assets/img/clock5.png" alt="action">
    <img :style="{display: store.actionFrame===6 ? 'block' : 'none'}" src="../../../assets/img/clock6.png" alt="action">
    <img :style="{display: store.actionFrame===7 ? 'block' : 'none'}" src="../../../assets/img/clock7.png" alt="action">
    <img :style="{display: store.actionFrame===8 ? 'block' : 'none'}" src="../../../assets/img/clock8.png" alt="action">
    <img :style="{display: store.actionFrame===9 ? 'block' : 'none'}" src="../../../assets/img/clock9.png" alt="action">
    <img :style="{display: store.actionFrame===10 ? 'block' : 'none'}" src="../../../assets/img/clock10.png" alt="action">
    <img :style="{display: store.actionFrame===11 ? 'block' : 'none'}" src="../../../assets/img/clock11.png" alt="action">
    <img :style="{display: store.actionFrame===12 ? 'block' : 'none'}" src="../../../assets/img/clock12.png" alt="action">
    <img :style="{display: store.actionFrame===13 ? 'block' : 'none'}" src="../../../assets/img/clock13.png" alt="action">
    <img :style="{display: store.actionFrame===14 ? 'block' : 'none'}" src="../../../assets/img/clock14.png" alt="action">
    <img :style="{display: store.actionFrame===15 ? 'block' : 'none'}" src="../../../assets/img/clock15.png" alt="action">
    <img :style="{display: store.actionFrame===16 ? 'block' : 'none'}" src="../../../assets/img/clock16.png" alt="action">
    <img :style="{display: store.actionFrame===17 ? 'block' : 'none'}" src="../../../assets/img/clock17.png" alt="action">
    <img :style="{display: store.actionFrame===18 ? 'block' : 'none'}" src="../../../assets/img/clock18.png" alt="action">
    <img :style="{display: store.actionFrame===19 ? 'block' : 'none'}" src="../../../assets/img/clock19.png" alt="action">
    <img :style="{display: store.actionFrame===20 ? 'block' : 'none'}" src="../../../assets/img/clock20.png" alt="action">
    <img :style="{display: store.actionFrame===21 ? 'block' : 'none'}" src="../../../assets/img/clock21.png" alt="action">
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
.container {
  position: absolute;
  left: 300px;
  top: 300px;
}
</style>
