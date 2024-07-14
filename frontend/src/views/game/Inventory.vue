<script lang="ts">
import { defineComponent, ref } from 'vue'
import GameClient from '../../net/GameClient'
import { ClientPacket, type InventoryUpdate, type InvItem } from '@/net/packets'
import ItemSlot from '@/views/game/ItemSlot.vue'
import Item from '@/views/game/Item.vue'
import Window from '@/views/game/Window.vue'

export default defineComponent({
  name: 'Inventory',
  components: { Window, ItemSlot, Item },
  props: {
    inventory: {
      type: Object as () => InventoryUpdate,
      required: true
    },
  },
  setup(props) {
    const visible = ref(true)

    const onClose = () => {
      GameClient.instance?.send('invclose', {
        iid: props.inventory.id
      })
    }

    // клик по пустому слоту (класть вещь в инвентарь)
    const onSlotClick = (x: number, y: number, ox: number, oy: number) => {
      console.log('onSlotClick', x, y)
      GameClient.instance?.send(ClientPacket.ITEM_CLICK, {
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
      console.log('itemClick', item.c)
      GameClient.instance?.send(ClientPacket.ITEM_CLICK, {
        id: item.id,
        iid: props.inventory.id,
        x: Math.floor(ox / 31),
        y: Math.floor(oy / 31),
        ox,
        oy
      })
    }

    return { visible, onClose, onSlotClick, onItemClick }
  }
})
</script>

<template>
  <window
    v-if="visible"
    :id="inventory.id"
    :inner-height="inventory.h * 31"
    :inner-width="inventory.w * 31"
    :title="inventory.t"
    @close="onClose"
  >
    <div v-for="y in inventory.h" :key="y">
      <div v-for="x in inventory.w" :key="x">
        <item-slot :left="16 + (x-1) * 31" :top="22 + (y-1) * 31" :x="x-1" :y="y-1"
                   @slotClick="onSlotClick"></item-slot>
      </div>
    </div>

    <div v-for="item in inventory.l" :key="item.id">
      <item :item="item" @itemClick="onItemClick"></item>
    </div>
  </window>
</template>