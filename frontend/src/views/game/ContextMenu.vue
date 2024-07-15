<script lang="ts" setup>
import ContextMenuButton from '@/views/game/ContextMenuButton.vue'
import { useGameStore } from '@/stores/gameStore'
import { computed } from 'vue'

const posX = computed(() => store.contextMenuPosX)
const posY = computed(() => store.contextMenuPosY)

const store = useGameStore()

const list = computed(() => store.contextMenu?.l || [])

</script>

<template>
  <transition-group :style="{left: posX+'px', top: posY+'px'}" class="context-menu-container" name="spiral" tag="div">
    <context-menu-button
      v-for="(c, idx) in list"
      :key="idx"
      :caption="c"
      :index="idx"
      class="action-button">
    </context-menu-button>
  </transition-group>
</template>

<style lang="scss">
.clear-button {
  position: absolute;
  left: 300px;
  top: 400px;
  pointer-events: auto;
}

.context-menu-container {
  pointer-events: auto;
  opacity: 1;
  position: relative;
}

.action-button {
  position: absolute;
  transform: translate(var(--x1), var(--y1));
  animation-duration: 0.5s;
  animation-direction: alternate;
  animation-timing-function: linear;
}

.spiral-enter-active {
  animation-name: cm-move;
}

.spiral-leave-active {
  animation-duration: 0.2s;
  animation-name: cm-move-hide;
}

@keyframes cm-move {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  33% {
    transform: translate(var(--x3), var(--y3));
    opacity: 0.2;
  }
  66% {
    transform: translate(var(--x2), var(--y2));
    opacity: 0.66;
  }
  100% {
    transform: translate(var(--x1), var(--y1));
    opacity: 1;
  }
}

@keyframes cm-move-hide {
  100% {
    transform: translate(var(--x1), var(--y1));
    opacity: 0;
  }
  0% {
    transform: translate(var(--x1), var(--y1));
    opacity: 1;
  }
}

</style>