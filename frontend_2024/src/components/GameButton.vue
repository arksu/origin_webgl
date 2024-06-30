<script lang="ts">
import { defineComponent, reactive } from 'vue'

export default defineComponent({
  name: 'GameButton',
  props: {
    tooltip: String,
    fontColor: String,
    borderColor: String,
    backColor: String
  },
  emits: ['click'],
  setup(props) {
    const style = reactive({
      '--border-color': props.borderColor,
      '--back-color': props.backColor,
      '--font-color': props.fontColor
    })
    return { style }
  }
})
</script>

<template>
  <div class="game-button" :style="style" @click.prevent="$emit('click')">
    <div class="inline">
      <slot />
    </div>
    <span class="tooltip-game-button">
      {{ tooltip }}
    </span>
  </div>
</template>

<style scoped lang="scss">
.game-button {
  margin: 2px;
  width: 46px;
  height: 46px;
  border: solid 3px;
  border-radius: 6px;
  border-color: var(--border-color);
  background-color: var(--back-color);
  pointer-events: auto;
  position: relative;
  cursor: pointer;
}

.game-button:hover {
  filter: brightness(120%);
}

.game-button:hover span {
  visibility: visible;
}

.inline {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 20px;
  color: var(--font-color);
  text-transform: uppercase;
}

</style>