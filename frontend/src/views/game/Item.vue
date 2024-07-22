<script lang="ts">
import { defineComponent } from 'vue'
import { ClientPacket, type InvItem } from '@/net/packets'
import GameClient from '@/net/GameClient'

export default defineComponent({
  name: 'Item',
  props: {
    item: {
      type: Object as () => InvItem,
      required: true
    },
    inventoryId: {
      type: Number,
      required: true
    }
  },
  emits: ['itemClick'],
  setup(props, { emit }) {

    const onClick = (e: MouseEvent) => {
      emit('itemClick', props.item, e.offsetX, e.offsetY)
    }

    const onContextmenu = (e: MouseEvent) => {
      console.log('onContextmenu', e)
      GameClient.instance?.send(ClientPacket.ITEM_RIGHT_CLICK, {
        id: props.item.id,
        iid: props.inventoryId
      })
    }

    return { onClick, onContextmenu }
  }
})
</script>

<template>
  <img
    :src="'/assets/game/' + item.icon"
    :style="'left: ' + (17 + item.x * 31) + 'px; top: ' + (23 + item.y * 31) + 'px;'"
    alt="item"
    class="item-image"
    @click.prevent="onClick"
    @contextmenu.prevent.stop="onContextmenu"
  >
</template>

<style lang="scss" scoped>
.item-image {
  position: absolute;
  cursor: pointer;
}
</style>
