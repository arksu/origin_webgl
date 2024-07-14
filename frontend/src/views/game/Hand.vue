<script lang="ts">
import { defineComponent } from 'vue'
import type { HandData } from '@/net/packets'

/**
 * предмет который держим в руке (перетаскивание между инвентарями)
 */
export default defineComponent({
  name: 'Hand',
  props: {
    hand: {
      type: Object as () => HandData,
      required: true,
    },
    left: {
      type: Number,
      required: true
    },
    top: {
      type: Number,
      required: true
    }
  }
})
</script>

<template>
  <div
    :style="'left: ' + (left - hand.mx) + 'px; top: ' + (top - hand.my) + 'px;'"
    class="tooltip">
    <img
      :src="'/assets/game' + hand.icon"
      alt="hand"
      style="display: block"
    />
  </div>
</template>

<style lang="scss" scoped>
.tooltip {
  display: inline-block;
  pointer-events: none;
  position: absolute;
  z-index: 9999;
  padding: 0;
}
</style>
