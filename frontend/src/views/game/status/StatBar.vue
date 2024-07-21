<script lang="ts">
import { defineComponent } from 'vue'
import Tooltip from '@/components/Tooltip.vue'

/**
 * бар для отображения статуса нашего перса (стамина, хп и прочее)
 */
export default defineComponent({
  name: 'StatBar',
  components: { Tooltip },
  props: {
    percent1: Number,
    percent2: Number,
    tooltip: String,
    frameColor: String,
    barBackColor: String,
    bar1Color: String,
    bar2Color: String
  },
  setup() {
    return {}
  }
})
</script>

<template>
  <div :style="{'border-color' : frameColor}" class="frame">
    <div :style="{'background-color' : barBackColor}" class="bar-back"></div>
    <div :style="{width: percent1 + '%', 'background-color': bar1Color}" class="bar1"></div>
    <div :style="{width: percent2 + '%', 'background-color': bar2Color}" class="bar2"></div>
    <tooltip>{{ tooltip }}</tooltip>
  </div>
</template>

<style lang="scss" scoped>
.frame {
  box-sizing: content-box;
  position: relative;
  margin-top: 8px;
  height: 5px;
  width: 120px;
  border: 2px solid;
  border-radius: 10px;
  pointer-events: auto;
  display: inline-block;
  margin-right: 20px;
}

.frame:hover span {
  visibility: visible;
}

.frame .bar-back {
  position: absolute;
  width: 100%;
  height: 5px;
  border-radius: 10px;
}

.frame .bar1, .frame .bar2 {
  position: absolute;
  height: 5px;
  border-radius: 10px;
}
</style>
