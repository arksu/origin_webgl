<script lang="ts">
import {defineComponent} from 'vue'
import type { InvItem } from '@/net/packets'

export default defineComponent({
  name: "Item",
  props: {
    item: {
      type: Object as () => InvItem,
    },
  },
  emits: ['itemClick'],
  setup(props, {emit}) {

    const onClick = (e: MouseEvent) => {
      emit('itemClick', props.item, e.offsetX, e.offsetY)
    }

    return {onClick}
  }
})
</script>

<template>
  <img class="item-image"
       alt="item"
       :style="'left: ' + (17 + item.x * 31) + 'px; top: ' + (23 + item.y * 31) + 'px;'"
       v-if="item !== undefined"
       :src="'/assets/game/' + item.icon"
       @click.prevent="onClick">
</template>

<style scoped lang="scss">
.item-image {
  position: absolute;
  cursor: pointer;
}
</style>
