<template>
  <window
      v-if="visible"
      :id="inventory.id"
      :title="inventory.t"
      :width="32 + inventory.w * 31"
      :height="38 + inventory.h * 31"
      @close="onClose"
  >
    <div v-for="y in inventory.h">
      <div v-for="x in inventory.w">
        <item-slot :x="x-1" :y="y-1" :left="16 + (x-1) * 31" :top="22 + (y-1) * 31"
                   @slotClick="onSlotClick"></item-slot>
      </div>
    </div>

    <div v-for="item in inventory.l">
      <item :item="item" @itemClick="onItemClick"></item>
    </div>
  </window>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import Window from "./Window.vue";
import ItemSlot from "./ItemSlot.vue";
import Item from "./Item.vue";
import {InventoryUpdate, InvItem} from "../../net/packets";
import GameClient from "../../net/GameClient";

export default defineComponent({
  name: "Inventory",
  components: {Window, ItemSlot, Item},
  props: {
    inventory: {
      type: Object as () => InventoryUpdate,
      required: true
    }
  },
  setup(props) {
    const visible = ref(true)

    const onClose = () => {
      GameClient.remoteCall("invclose", {
        iid: props.inventory.id
      })
    }

    // клик по пустому слоту (класть вещь в инвентарь)
    const onSlotClick = (x: number, y: number, ox: number, oy: number) => {
      console.log('onSlotClick', x, y)
      GameClient.remoteCall("itemclick", {
        id: 0, // ни в какую вещь не попали (это пустой слот)
        iid: props.inventory.id,
        x: x,
        y: y,
        ox,
        oy
      })
    }

    // клик по вещи (взять ее)
    const onItemClick = (item: InvItem, ox: number, oy: number) => {
      console.log("itemClick", item.c)
      GameClient.remoteCall("itemclick", {
        id: item.id,
        iid: props.inventory.id,
        x: Math.floor(ox / 31),
        y: Math.floor(oy / 31),
        ox,
        oy
      })
    }

    return {visible, onClose, onSlotClick, onItemClick}
  },
})
</script>

