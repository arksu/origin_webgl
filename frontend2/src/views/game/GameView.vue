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
import Render from "../../game/Render";
import {useGameStore} from "../../store/game";
import {useRoute, useRouter} from "vue-router";
import router from "../../router";
import {RouteNames} from "../../router/routeNames";
import {useMainStore} from "../../store/main";

export default defineComponent({
  name: "GameView",
  setup() {
    const route = useRoute()
    const active = ref(false)
    const store = useMainStore()
    const gameStore = useGameStore()

    onMounted(() => {
      const client = GameClient.createNew()
      client.onConnect = () => {
        // берем токен из параметров роута (туда положили при переходе из списка персонажей)
        const token = route.params.token
        console.log('token', token)
        // шлем запрос с токеном на сервер для первичной авторизации и активации токена
        GameClient.remoteCall('token', {token})
            .then(r => {
              active.value = true
              gameStore.selectedCharacterId = r.charId
              GameClient.data.selectedCharacterId = r.charId
              Render.start()
            })
      }
      client.onError = m => {
        active.value = false
        console.error(m)
        Render.stop()
        store.onGameError(m)
      }
      client.onDisconnect = () => {
        active.value = false
        Render.stop()
        router.push({name: RouteNames.LOGIN})
      }
    })

    onUnmounted(() => {
      Render.stop()
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
