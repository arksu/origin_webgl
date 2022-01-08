<template>
  <window
      v-if="visible"
      :id="33"
      title="Craft"
      :inner-width="100"
      :inner-height="200"
      @close="visible = false"
  >
  </window>
</template>

<script lang="ts">
import Window from "./Window.vue";

import {defineComponent} from "vue";

export default defineComponent({
  name: "Craft",
  components: {Window},
  props: {
    visible: Boolean
  },
  setup() {

  }
})
</script>

<style scoped lang="scss">

</style>