<template>
  <div class="item-back" :style="'left: ' + x + 'px; top: ' + y + 'px;'"></div>
</template>

<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "ItemSlot",
  props: {
    x: Number,
    y: Number,
  },
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