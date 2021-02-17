<template>
  <Window v-if="isVisible" :title="title" @close="isVisible = !isVisible" :width="32 + 4 * 35" :height="38 + 4 * 35">
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
import {InvItem} from "@/net/Packets";

export default defineComponent({
  name: "Inventory",
  components: {Window, Item},
  props: {
    title: String
  },
  data() {
    return {
      isVisible: true as boolean,
      items: [] as InvItem[][]
    }
  },
  methods: {},
  mounted() {
    // if (Client.instance.playerInventory !== undefined) {
    //   let d = Client.instance.playerInventory
    //   this.t
    // }
  }
})
</script>

<style scoped>

</style>