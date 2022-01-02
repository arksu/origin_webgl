<template>
  <div class="item-back" :style="'left: ' + left + 'px; top: ' + top + 'px;'" @click.prevent="onClick"></div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ItemSlot",
  props: {
    left: Number,
    top: Number,
    x: Number,
    y: Number,
  },
  emits: ['slotClick'],
  setup(props, {emit}) {

    const onClick = (e: MouseEvent) => {
      // передаем слот куда был клик, а также координаты мыши внутри слота
      emit('slotClick', props.x, props.y, e.offsetX, e.offsetY)
    }

    return {onClick}
  }
})
</script>

<style scoped lang="scss">
.item-back {
  position: absolute;
  width: 32px;
  height: 32px;
  background-image: url('../../../assets/img/inventory_slot.png');
  background-repeat: no-repeat;
  padding-top: 2px;
}
</style>
