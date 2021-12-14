<template>
  <div class="item-back" :style="'left: ' + left + 'px; top: ' + top + 'px;'" @click="click"></div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "ItemSlot",
  props: {
    left: Number,
    top: Number,
    x: Number,
    y: Number,
  },
  emits: {
    slotClick: null
  },
  methods: {
    click(e: MouseEvent) {
      console.log(e)
      e.preventDefault()
      this.$emit('slotClick', this.x, this.y, e.offsetX, e.offsetY)
    }
  }
})
</script>

<style scoped>
.item-back {
  position: absolute;
  width: 32px;
  height: 32px;
  background-image: url('/assets/inventory_slot.png');
  background-repeat: no-repeat;
  padding-top: 2px;
}
</style>