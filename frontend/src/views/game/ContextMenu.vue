<script lang="ts" setup>
import ContextMenuButton from '@/views/game/ContextMenuButton.vue'
import { useGameStore } from '@/stores/gameStore'
import { onMounted, ref } from 'vue'

const props = defineProps<{
  x: number,
  y: number,
}>()

// const posX = ref(props.x - 150)
// const posY = ref(props.y - 150)
const posX = ref(400)
const posY = ref(400)

onMounted(() => {
})

const store = useGameStore()

const list = ref([
  'chop',
  'take',
  'some'
])

const getButtonStyle = (idx: number) => {
  const angle = (idx / list.value.length) * Math.PI - Math.PI/4
  const radius = 70 // Adjust the radius for the spiral
  const x =  Math.cos(angle) * radius
  const y =  Math.sin(angle) * radius

  return {
    '--x': `${x}px`,
    '--y': `${y}px`
  }

  // return {
  //   transform: `translate(${x}px, ${y}px)`,
  //   opacity: list.value.length ? 1 : 0,
  //   transitionDelay: `${idx * 100}ms`,
  // }
}

const onClear = () => {
  console.log('onClear')
  if (list.value.length > 0) {
    list.value = []
  } else {
    list.value = [
      'chop',
      'take',
      'some'
    ]
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
  width: 300px;
  height: 300px;
  background: #1a4f72;
  opacity: 1;
  position: relative;
}


.action-button {
  position: absolute;
  transform: translate(var(--x), var(--y));
}

.spiral-move,
.spiral-enter-active,
.spiral-leave-active {
  transition: transform 0.5s ease, opacity 0.5s ease;
}

.spiral-enter-from,
.spiral-leave-to {
  transform: translate(0, 0);
  opacity: 0;
}

.spiral-enter-to,
.spiral-leave-from {
  transform: translate(var(--x), var(--y));
  opacity: 1;
}


</style>