<template>
  <img class="item-image"
       alt="item"
       :style="'left: ' + (17 + item.x * 31) + 'px; top: ' + (23 + item.y * 31) + 'px;'"
       v-if="item !== undefined"
       :src="'/assets/' + item.icon"
       @click="click">
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Item",
  props: {
    item: Object,
  },
  emits: {
    itemClick: null
  },
  computed: {},
  methods: {
    click(e: MouseEvent) {
      console.log(e)
      e.preventDefault()
      this.$emit('itemClick', this.item, e.offsetX, e.offsetY)
    }
  }
})
</script>

<style scoped>


.item-image {
  position: absolute;
  cursor: pointer;
}
</style>