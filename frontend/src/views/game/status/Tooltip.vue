<template>
  <span class="tooltip-text">{{ text }}</span>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Tooltip",
  props: {
    text: String
  },
})
</script>

<style scoped lang="scss">
.tooltip-text {
  visibility: hidden;
  width: 100px;
  background-color: black;
  color: #fff;
  text-align: center;
  border-radius: 6px;
  padding: 5px 0;

  /* Position the tooltip */
  position: absolute;
  z-index: 100;

  margin-top: 10px;
  margin-left: 10px;
  font-size: 12px;
}

</style>
