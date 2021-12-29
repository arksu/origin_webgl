<template>
  <div class="frame" :style="{'border-color' : frameColor}">
    <div class="bar-back" :style="{'background-color' : barBackColor}"></div>
    <div class="bar" :style="{width: percent + '%', 'background-color': barColor}"></div>
    <tooltip :text="tooltip"/>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import Tooltip from "./Tooltip.vue";

/**
 * бар для отображения статуса нашего перса (стамина, хп и прочее)
 */
export default defineComponent({
  name: "StatBar",
  components: {Tooltip},
  props: {
    percent: Number,
    tooltip: String,
    frameColor: String,
    barBackColor: String,
    barColor: String,
  },
  setup() {

    return {}
  }
})
</script>

<style scoped lang="scss">

.frame {
  position: relative;
  height: 5px;
  width: 120px;
  border: 2px solid;
  border-radius: 10px;
  pointer-events: auto;
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

.frame .bar {
  position: absolute;
  height: 5px;
  border-radius: 10px;
}
</style>
