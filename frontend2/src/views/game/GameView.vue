<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, onMounted, onUnmounted, ref} from 'vue'
import GameClient from "../../net/GameClient";
import Game from "../../game/Game";

export default defineComponent({
  name: "GameView",
  setup() {
    const active = ref(false)

    onMounted(() => {
      const client = GameClient.createNew()
      client.onConnect = () => {
        Game.start()
        active.value = true
      }
    })

    onUnmounted(() => {
      Game.stop()
    })

    return {active}
  }
})
</script>

<style scoped>

</style>