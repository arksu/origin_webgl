<template>
  <div class="container">
    <div>
      {{ name }}
    </div>
    <div v-if="id !== 0"></div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: 'CharacterRow',
  props: {
    name: {
      type: String,
      required: true
    },
    id: {
      type: Number,
      required: true
    }
  },
  setup() {

  }
})
</script>
<style scoped lang="scss">

.container {
  width: 100%;
  display: table;
  margin: 10px 0;
}

.row {

}

</style>