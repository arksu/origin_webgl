<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'ItemSlot',
  props: {
    left: Number,
    top: Number,
    x: Number,
    y: Number
  },
  emits: ['slotClick'],
  setup(props, { emit }) {

    const onClick = (e: MouseEvent) => {
      console.log('onClick', e)
      // передаем слот куда был клик, а также координаты мыши внутри слота
      emit('slotClick', props.x, props.y, e.offsetX, e.offsetY)
    }

    return { onClick }
  }
})
</script>

<template>
  <div :style="'left: ' + left + 'px; top: ' + top + 'px;'"
       class="item-back"
       @click.prevent="onClick">
  </div>
</template>

<style lang="scss" scoped>
.item-back {
  position: absolute;
  width: 32px;
  height: 32px;
  background-image: url('../../../assets/img/inventory_slot.png');
  background-repeat: no-repeat;
  padding-top: 2px;
}
</style>
