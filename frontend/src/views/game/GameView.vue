<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <div class="game-ui" v-if="active">
    <avatar/>
    <stats/>
    <chat/>
    <action-hour-glass/>

    <!-- inventories-->
    <inventory v-for="i in gameStore.inventories" :inventory="i"/>

    <hand v-if="gameStore.hand !== undefined" :left="mouseX" :top="mouseY" :hand="gameStore.hand"/>

    <day-time v-if="gameStore.time !== undefined"/>
  </div>

</template>

<script lang="ts">
import {defineComponent, onMounted, onUnmounted, ref} from 'vue'
import Avatar from "./Avatar.vue";
import Stats from "./status/Stats.vue";
import Chat from "./Chat.vue";
import ActionHourGlass from "./ActionHourGlass.vue";
import Inventory from "./Inventory.vue";
import Hand from "./Hand.vue";
import DayTime from "./DayTime.vue";
import GameClient from "../../net/GameClient";
import Render from "../../game/Render";
import {useGameStore} from "../../store/game";
import {useRoute} from "vue-router";
import router from "../../router";
import {RouteNames} from "../../router/routeNames";
import {useMainStore} from "../../store/main";

/**
 * игровой вид, рендер и весь UI для игры
 */
export default defineComponent({
  name: "GameView",
  components: {Avatar, Stats, Chat, ActionHourGlass, Inventory, Hand, DayTime},
  setup() {
    const route = useRoute()
    const active = ref(false)
    const store = useMainStore()
    const gameStore = useGameStore()

    const mouseX = ref(0)
    const mouseY = ref(0)

    const onMouseMove = (e: MouseEvent) => {
      mouseX.value = e.clientX
      mouseY.value = e.clientY
    }
    onMounted(() => {
      // подключаемся к вебсокету на игровом сервере
      const client = GameClient.createNew()
      client.onConnect = () => {
        // берем токен из параметров роута (туда положили при переходе из списка персонажей)
        const token = route.params.token
        console.log('token', token)
        // запустим старт рендера, загрузку атласов
        Render.start()
            // как только атласы загружены
            .then(() => {
              // шлем запрос с токеном на сервер для первичной авторизации и активации токена
              GameClient.remoteCall('token', {token})
                  .then(r => {
                    window.addEventListener('mousemove', onMouseMove)
                    active.value = true
                    gameStore.selectedCharacterId = r.characterId
                    GameClient.data.selectedCharacterId = r.characterId
                  })
            })
            // в случае сбоя запуска рендера
            .catch((e) => {
              active.value = false
              Render.stop()
              store.onGameError(e)
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
      window.removeEventListener('mousemove', onMouseMove)
      GameClient.instance?.disconnect()
      Render.stop()
    })

    return {active, mouseX, mouseY, store, gameStore}
  }
})
</script>

<style scoped lang="scss">

.game-ui {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  user-select: none;
}

</style>
