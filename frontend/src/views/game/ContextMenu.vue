<script lang="ts" setup>
import ContextMenuButton from '@/views/game/ContextMenuButton.vue'
import { useGameStore } from '@/stores/gameStore'
import { onMounted, onUnmounted, ref } from 'vue'

const props = defineProps<{
  x: number,
  y: number,
}>()

const posX = ref(props.x)
const posY = ref(props.y)

onMounted(() => {
})

const store = useGameStore()

const initial = () => {
  return [
    'take',
    'take',
    'take',
    'some'
  ]
}

const list = ref<string[]>([])

onMounted(() => {
  list.value = store.contextMenu?.l || []
})

const getButtonStyle = (idx: number) => {
  let radius1 = 50 +list.value.length * 10
  let radius2 = 55
  let radius3 = 45
  const offset = list.value.length * 0.3
  let angle1 = (idx / list.value.length) * Math.PI - offset
  let angle2 = (idx / list.value.length) * Math.PI - offset - 0.72
  let angle3 = (idx / list.value.length) * Math.PI - offset - 1.9

  const x1 = Math.cos(angle1) * radius1
  const y1 = Math.sin(angle1) * radius1

  const x2 = Math.cos(angle2) * radius2
  const y2 = Math.sin(angle2) * radius2
  const x3 = Math.cos(angle3) * radius3
  const y3 = Math.sin(angle3) * radius3


  return {
    '--x1': `${x1}px`,
    '--y1': `${y1}px`,
    '--x2': `${x2}px`,
    '--y2': `${y2}px`,
    '--x3': `${x3}px`,
    '--y3': `${y3}px`
  }
}

const onClear = () => {
  console.log('onClear')
  if (list.value.length > 0) {
    list.value = []
  } else {
    list.value = initial()
  }
}

</script>

<template>
  <button class="clear-button" @click.prevent="onClear">clear</button>
  <transition-group :style="{left: posX+'px', top: posY+'px'}" class="context-menu-container" name="spiral" tag="div">
    <context-menu-button
      v-for="(c, idx) in list"
      :key="idx"
      :caption="c"
      :style="getButtonStyle(idx)" class="action-button"></context-menu-button>
  </transition-group>
</template>

<style lang="scss" scoped>
.clear-button {
  position: absolute;
  left: 300px;
  top: 400px;
  pointer-events: auto;
}

.context-menu-container {
  pointer-events: auto;
  //width: 300px;
  //height: 300px;
  //background: #1a4f72;
  opacity: 1;
  position: relative;
}

.action-button {
  position: absolute;
  //transition: transform 0.5s ease, opacity 0.5s ease;
  transform: translate(var(--x1), var(--y1));
  //opacity: 1;
  animation-duration: 0.4s;
  //animation-name: cm-move;
  animation-direction: alternate;
  //animation-fill-mode: both;
  //animation-timing-function: ease-in-out;
  animation-timing-function: linear;
}

.spiral-enter-active {
  animation-name: cm-move;
}

.spiral-leave-active {
  animation-name: cm-move-leave;
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

@keyframes cm-move-leave {
  100% {
    transform: translate(0, 0);
    opacity: 0;
  }
  66% {
    transform: translate(var(--x3), var(--y3));
    opacity: 0.2;
  }
  33% {
    transform: translate(var(--x2), var(--y2));
    opacity: 0.66;
  }
  0% {
    transform: translate(var(--x1), var(--y1));
    opacity: 1;
  }
}

</style>