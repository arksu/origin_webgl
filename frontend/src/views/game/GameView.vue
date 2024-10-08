<script lang="ts">
import { defineComponent, onMounted, onUnmounted, ref } from 'vue'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'
import { useAuthStore } from '@/stores/authStore'
import { useGameStore } from '@/stores/gameStore'
import GameClient from '@/net/GameClient'
import GameButton from '@/components/GameButton.vue'
import Render from '@/game/Render'
import GameData from '@/net/GameData'
import { type AuthorizeTokenResponse, ClientPacket } from '@/net/packets'
import Chat from '@/views/game/Chat.vue'
import Inventory from '@/views/game/Inventory.vue'
import Hand from '@/views/game/Hand.vue'
import ContextMenu from '@/views/game/ContextMenu.vue'
import { mouse } from '@/game/Mouse'
import ActionHourGlass from '@/views/game/ActionHourGlass.vue'
import Stats from '@/views/game/status/PlayerStats.vue'
import DayTime from '@/views/game/DayTime.vue'
import Craft from '@/views/game/Craft.vue'

/**
 * игровой вид, рендер и весь UI для игры
 */
export default defineComponent({
  name: 'GameView',
  components: { Craft, DayTime, Stats, ActionHourGlass, ContextMenu, Hand, GameButton, Chat, Inventory },
  setup() {
    const isActive = ref(false)
    const authStore = useAuthStore()
    const gameStore = useGameStore()

    const mouseX = ref(0)
    const mouseY = ref(0)

    let client: GameClient | undefined = undefined
    let render: Render | undefined = undefined

    const onMouseMove = (e: MouseEvent) => {
      mouseX.value = e.clientX
      mouseY.value = e.clientY
      mouse.x = e.clientX
      mouse.y = e.clientY
    }

    onMounted(() => {
      console.log('ws token', authStore.websocketToken)

      const token = authStore.websocketToken
      if (token) {
        // подключаемся к вебсокету на игровом сервере
        const data = new GameData()
        render = new Render(data)
        client = new GameClient(render)
        render.client = client

        client.onConnect = () => {
          render?.init()
          // загружаем атласы
          render?.load()
            // как только атласы загружены
            .then((r) => {
              console.log('assets loaded', r)
              // шлем запрос с токеном на сервер для первичной авторизации и активации токена
              client?.send('token', { token })
                .then((r) => {
                  const response = r as AuthorizeTokenResponse
                  render?.setup()
                  gameStore.client = client!!
                  isActive.value = true
                  data.selectedCharacterId = response.characterId
                  window.addEventListener('mousemove', onMouseMove)
                  console.log('proto version', response.proto)
                })
                .catch((e) => {
                  stop()
                  authStore.setError(e.toString(), false)
                  router.push({ name: RouteNames.CHARACTERS })
                })
            })
        }

        client.onError = (errorMessage) => {
          console.error(errorMessage)
          stop()
          authStore.setError(errorMessage)
          router.push({ name: RouteNames.CHARACTERS })
        }

        client.onDisconnect = () => {
          stop()
          router.push({ name: RouteNames.CHARACTERS })
        }
      } else {
        router.replace({ name: RouteNames.CHARACTERS })
      }
    })

    onUnmounted(() => {
      if (client !== undefined) {
        client.onDisconnect = undefined
        client.disconnect()
        client = undefined
      }
      if (render !== undefined) {
        render.stop()
        render = undefined
      }
      gameStore.$reset()
      window.removeEventListener('mousemove', onMouseMove)
    })

    function stop() {
      isActive.value = false
      render?.stop()
      gameStore.client = undefined
    }

    const toggleCraftWindow = () => {
      console.log('toggleCraftWindow')
      gameStore.craft.isOpened = !gameStore.craft.isOpened
    }

    const toggleInventory = () => {
      render?.toggleInventory()
    }

    const liftAction = () => {
      GameClient.instance?.send(ClientPacket.ACTION, {
        name: 'lift'
      })
    }

    return {
      isActive,
      mouseX,
      mouseY,
      gameStore,
      toggleCraftWindow,
      toggleInventory,
      liftAction
    }
  }
})
</script>

<template>
  <div v-if="!isActive" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <div v-if="isActive" class="game-ui">
    <stats />
    <chat />
    <action-hour-glass />
    <day-time />

    <!-- inventories-->
    <inventory v-for="i in gameStore.inventories" :key="i.id" :inventory="i" />

    <craft v-if="gameStore.craft.isOpened" @close="toggleCraftWindow" />

    <context-menu />

    <!-- player hand-->
    <hand v-if="gameStore.hand !== undefined" :hand="gameStore.hand" :left="mouseX" :top="mouseY" />

    <div style="right: 0; bottom: 0; position: absolute">

      <!--  Lift  -->
      <game-button
        back-color="#315B5E"
        border-color="#25484B"
        font-color="#142628"
        tooltip="Lift object"
        @click="liftAction">
        <i class="fas fa-arrow-up-from-bracket"></i>
      </game-button>

      <!--  Craft  -->
      <game-button
        back-color="#315B5E"
        border-color="#25484B"
        font-color="#142628"
        tooltip="Craft"
        @click="toggleCraftWindow">
        <i class="fas fa-tools"></i>
      </game-button>

      <!--  Inventory  -->
      <game-button
        back-color="#315B5E"
        border-color="#25484B"
        font-color="#142628"
        tooltip="Inventory"
        @click="toggleInventory">
        <i class="fas fa-box"></i>
      </game-button>

      <!--  Logout  -->
      <game-button
        back-color="#683E36"
        border-color="#59322C"
        font-color="#301717"
        tooltip="Logout"
        @click="gameStore.logout()"
      >
        <i class="fas fa-sign-out-alt"></i>
      </game-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.game-ui {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  user-select: none;
}
</style>
