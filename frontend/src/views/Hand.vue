<template>
  <div
      :style="'left: ' + (left - ox) + 'px; top: ' + (top - oy) + 'px;'"
      class="tooltip">
    <img
        style="display: block"
        :src="'/assets' +$store.state.hand.icon"
    />
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Client from "@/net/Client";

export default defineComponent({
  name: "Hand",
  props: {
    ox: Number,
    oy: Number
  },
  data() {
    return {
      left: 0 as number,
      top: 0 as number
    }
  },
  methods: {
    onMove(e: MouseEvent) {
      this.left = e.clientX
      this.top = e.clientY
    }
  },
  mounted() {
    console.warn("hand mount")
    console.log(window.onmousemove)
    window.onmousemove = this.onMove
    this.left = Client.instance.mouseX
    this.top = Client.instance.mouseY
  },
  unmounted() {
    console.warn("hand unmount")
    window.onmousemove = null
  }
})
</script>

<style scoped>

.tooltip {
  display: inline-block;
  pointer-events: none;
  position: absolute;
  z-index: 9999;
  padding: 0;
}

</style>