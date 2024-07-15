<script lang="ts" setup>
import { useGameStore } from '@/stores/gameStore'
import GameClient from '@/net/GameClient'
import { ClientPacket } from '@/net/packets'

const props = defineProps<{
  index: number
  caption: string
}>()

const store = useGameStore()

const onClick = () => {
  console.log('onClick')

  document.getElementById('context-menu-item-' + props.index)?.classList.add('selected')

  store.contextMenu = undefined
  GameClient.instance?.send(ClientPacket.CONTEXT_MENU_SELECT, {
    item: props.caption
  })
}

const len = store.contextMenu?.l.length || 1

const getButtonStyle = () => {
  let radius1 = 50 + len * 10
  let radius2 = 55
  let radius3 = 40
  const offset = len * 0.3
  let angle1 = (props.index / len) * Math.PI - offset
  let angle2 = (props.index / len) * Math.PI - offset - 0.72
  let angle3 = (props.index / len) * Math.PI - offset - 1.9

  const xOffset = 50
  const yOffset = 30

  const x1 = Math.cos(angle1) * radius1 - xOffset
  const y1 = Math.sin(angle1) * radius1 - yOffset
  const x2 = Math.cos(angle2) * radius2 - xOffset
  const y2 = Math.sin(angle2) * radius2 - yOffset
  const x3 = Math.cos(angle3) * radius3 - xOffset
  const y3 = Math.sin(angle3) * radius3 - yOffset

  return {
    '--x1': `${x1}px`,
    '--y1': `${y1}px`,
    '--x2': `${x2}px`,
    '--y2': `${y2}px`,
    '--x3': `${x3}px`,
    '--y3': `${y3}px`
  }
}

</script>

<template>
  <div :id="'context-menu-item-'+index" :style="getButtonStyle()" class="context-menu-button" @click.prevent="onClick">
    <p>{{ caption }}</p>
  </div>
</template>

<style lang="scss" scoped>
.context-menu-button {
  padding: 5px;
  color: #A8B087;
  border: 2px solid #9F935D;
  border-radius: 5px;
  background-color: #363E19;
  cursor: pointer;
}

.selected {
  animation-duration: 0.5s !important;
  animation-name: cm-move-leave !important;
}

@keyframes cm-move-leave {
  100% {
    transform: translate(-40px, -30px);
    opacity: 0;
  }
  0% {
    transform: translate(var(--x1), var(--y1));
    opacity: 1;
  }
}

</style>
