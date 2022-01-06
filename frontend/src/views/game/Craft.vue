<template>
  <window
      v-if="visible"
      :id="33"
      title="Craft"
      :width="300"
      :height="200"
      @close="visible = false"
  >
  </window>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Craft",
  props: {
    visible: Boolean
  },
  setup() {

  }
})
</script>

<style scoped lang="scss">

</style>