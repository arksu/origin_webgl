<template>
  <Window v-if="isVisible" title="Inventory" @close="isVisible = !isVisible">
    <div v-for="(line, rows) in items">
      <div class="item-back" v-for="(item, cols) in line">
        <Item :title="item" :x="16 + cols * 35" :y="22 + rows * 35"></Item>
      </div>
    </div>
  </Window>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Item from "@/views/Item.vue";
import Window from "@/views/Window.vue";

export default defineComponent({
  name: "Inventory",
  components: {Window, Item},
  data() {
    return {
      isVisible: true as boolean,
      items: [['s', 'd', 'e', 'w'], ['f', 'g', 't', '4'], ['s', 'd', 'e', 'w'], ['f', 'g', 't', '4']]
    }
  },
  methods: {}
})
</script>

<style scoped>

</style>