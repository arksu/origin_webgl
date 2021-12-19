<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <!-- pixi canvas, рулим через display чтобы Render мог найти элемент в DOM дереве при создании контекста PIXI -->
  <canvas id="game" v-bind:style="{display: active ? 'block' : 'none' }"></canvas>

</template>

<script lang="ts">
import {defineComponent, onMounted, onUnmounted, ref} from 'vue'
import GameClient from "../../net/GameClient";
import GameRender from "../../game/GameRender";

export default defineComponent({
  name: "GameView",
  setup() {
    const active = ref(false)

    onMounted(() => {
      const client = GameClient.createNew()
      client.onConnect = () => {
        // client.se


        active.value = true
        GameRender.start()
      }
    })

    onUnmounted(() => {
      GameRender.stop()
    })

    return {active}
  }
})
</script>

<style scoped lang="scss">

canvas {
  background-color: #4b3932;
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0;
  margin: 0;
}

</style>