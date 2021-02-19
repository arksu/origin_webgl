<template>
  <Window v-if="isVisible"
          :title="inv.t"
          @close="close"
          :width="32 + inv.w * 31"
          :height="38 + inv.h * 31">

    <div v-for="y in inv.h">
      <div v-for="x in inv.w">
        <ItemSlot :x="16 + (x-1) * 31" :y="22 + (y-1) * 31"></ItemSlot>
      </div>
    </div>

    <div v-for="item in inv.l">
      <Item :item="item" @itemClick="itemClick"></Item>
    </div>

  </Window>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Item from "@/views/Item.vue";
import Window from "@/views/Window.vue";
import {InvItem} from "@/net/Packets";
import ItemSlot from "@/views/ItemSlot.vue";
import Net from "@/net/Net";

export default defineComponent({
  name: "Inventory",
  components: {ItemSlot, Window, Item},
  props: {
    inv: Object
  },
  data() {
    return {
      isVisible: true as boolean,
    }
  },
  methods: {
    itemClick(item: InvItem) {
      console.log("itemClick", item.c)
      Net.remoteCall("itemclick", {
        id: item.id,
        iid: this.inv!!.id
      })
    },
    close() {
      Net.remoteCall("invclose", {
        iid: this.inv!!.id
      })
    }
  }
})
</script>

<style scoped>

</style>