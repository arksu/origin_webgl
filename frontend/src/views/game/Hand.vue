<template>
  <div
      :style="'left: ' + (left - hand.mx) + 'px; top: ' + (top - hand.my) + 'px;'"
      class="tooltip">
    <img
        alt="hand"
        style="display: block"
        :src="'/assets/game' + hand.icon"
    />
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {HandData} from "../../net/packets";

/**
 * предмет который держим в руке (перетаскивание между инвентарями)
 */
export default defineComponent({
  name: "Hand",
  props: {
    hand: Object as () => HandData,
    left: Number,
    top: Number,
  },
})
</script>

<style scoped lang="scss">
.tooltip {
  display: inline-block;
  pointer-events: none;
  position: absolute;
  z-index: 9999;
  padding: 0;
}
</style>
