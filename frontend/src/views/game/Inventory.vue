<template>
  <Window v-if="isVisible"
          :id="inv.id"
          :title="inv.t"
          @close="close"
          :width="32 + inv.w * 31"
          :height="38 + inv.h * 31">

    <div v-for="y in inv.h">
      <div v-for="x in inv.w">
        <ItemSlot :x="x-1" :y="y-1" :left="16 + (x-1) * 31" :top="22 + (y-1) * 31" @slotClick="slotClick"></ItemSlot>
      </div>
    </div>

    <div v-for="item in inv.l">
      <Item :item="item" @itemClick="itemClick"></Item>
    </div>

  </Window>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Item from "@/views/game/Item.vue";
import Window from "@/views/game/Window.vue";
import {InventoryUpdate, InvItem} from "@/net/Packets";
import ItemSlot from "@/views/game/ItemSlot.vue";
import Net from "@/net/Net";

export default defineComponent({
  name: "Inventory",
  components: {ItemSlot, Window, Item},
  props: {
    inv: Object as () => InventoryUpdate
  },
  data() {
    return {
      isVisible: true as boolean,
    }
  },
  methods: {
    itemClick(item: InvItem, ox: number, oy: number) {
      console.log("itemClick", item.c)
      Net.remoteCall("itemclick", {
        id: item.id,
        iid: this.inv!!.id,
        x: Math.floor(ox / 31),
        y: Math.floor(oy / 31),
        ox,
        oy
      })
    },
    slotClick(x: number, y: number, ox: number, oy: number) {
      console.log("slotClick", x, y, ox, oy)
      Net.remoteCall("itemclick", {
        id: 0,
        iid: this.inv!!.id,
        x: x,
        y: y,
        ox,
        oy
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
