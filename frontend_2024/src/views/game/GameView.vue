<script lang="ts">
import { defineComponent, onMounted, onUnmounted, ref } from 'vue'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'
import { useAuthStore } from '@/stores/authStore'
import { useGameStore } from '@/stores/gameStore'
import GameClient from '@/net/GameClient'
import GameButton from '@/components/GameButton.vue'

/**
 * игровой вид, рендер и весь UI для игры
 */
export default defineComponent({
  name: 'GameView',
  components: { GameButton },
  setup() {
    const isActive = ref(false)
    const authStore = useAuthStore()
    const gameStore = useGameStore()

    const mouseX = ref(0)
    const mouseY = ref(0)

    const onMouseMove = (e: MouseEvent) => {
      mouseX.value = e.clientX
      mouseY.value = e.clientY
    }
    onMounted(() => {
      console.log('ws token', authStore.websocketToken)
      if (authStore.websocketToken) {
        // подключаемся к вебсокету на игровом сервере
        const client = GameClient.createNew()

        client.onConnect = () => {
          isActive.value = true
        }

        client.onError = (errorMessage) => {
          isActive.value = false
          console.error(errorMessage)
          // Render.stop();
          authStore.setError(errorMessage)
          router.push({ name: RouteNames.LOGIN })
        }

        client.onDisconnect = () => {
          isActive.value = false
          // Render.stop();
          router.push({ name: RouteNames.LOGIN })
        }
      } else {
        router.replace({ name: RouteNames.CHARACTERS })
      }
    })

    onUnmounted(() => {
      if (GameClient.instance != undefined) {
        GameClient.instance.onDisconnect = undefined;
        GameClient.instance.disconnect();
      }
      gameStore.$reset();
      window.removeEventListener("mousemove", onMouseMove);
      // Render.stop();
    })

    const toggleCraftWindow = () => {
      console.log('toggleCraftWindow')
      // gameStore.craft.isOpened = !gameStore.craft.isOpened;
    }

    return {
      isActive,
      mouseX,
      mouseY,
      gameStore,
      toggleCraftWindow
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

  <div class="game-ui" v-if="isActive">
    <div style="right: 0; bottom: 0; position: absolute">
      <game-button
        tooltip="Logout"
        @click="gameStore.logout()"
        font-color="#301717"
        border-color="#59322C"
        back-color="#683E36"
      >
        <i class="fas fa-sign-out-alt"></i>
      </game-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.game-ui {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  user-select: none;
}
</style>
