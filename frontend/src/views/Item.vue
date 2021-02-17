<template>
  <div class="item-back" :style="style">
    <span>{{ title }}</span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Item",
  props: {
    x: Number,
    y: Number,
    title: String,
    q: Number
  },
  computed: {
    style(): string {
      return "left: " + this.x + "px; top: " + this.y + "px;"
    }
  }
})
</script>

<style scoped>
.item-back {
  position: absolute;
  width: 36px;
  height: 36px;
  background-image: url('/assets/inventory_slot.png');
  background-repeat: no-repeat;
  padding-top: 10px;
}
</style>