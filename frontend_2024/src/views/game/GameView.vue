<script lang="ts">
import { defineComponent, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'
import { useAuthStore } from '@/stores/authStore'
import { useGameStore } from '@/stores/gameStore'

/**
 * игровой вид, рендер и весь UI для игры
 */
export default defineComponent({
  name: 'GameView',
  components: {},
  setup() {
    const route = useRoute()
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

      } else {
        router.replace({ name: RouteNames.CHARACTERS })
      }
    })

    onUnmounted(() => {

    })

    const toggleCraftWindow = () => {
      console.log('toggleCraftWindow')
      // gameStore.craft.isOpened = !gameStore.craft.isOpened;
    }

    return {
      active: isActive,
      mouseX,
      mouseY,
      gameStore,
      toggleCraftWindow
    }
  }
})
</script>

<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <div class="game-ui" v-if="active">

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
