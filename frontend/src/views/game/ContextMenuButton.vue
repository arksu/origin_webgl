<script lang="ts" setup>
import { useGameStore } from '@/stores/gameStore'
import GameClient from '@/net/GameClient'
import { ClientPacket } from '@/net/packets'

const props = defineProps<{
  caption: string
}>()

const store = useGameStore()

const onClick = () => {
  console.log('onClick')
  store.contextMenu = undefined
  GameClient.instance?.send(ClientPacket.CONTEXT_MENU_SELECT, {
    item: props.caption
  })
}

</script>

<template>
  <div class="context-menu-button" @click.prevent="onClick">
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
</style>