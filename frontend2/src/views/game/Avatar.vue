<template>
  <div class="avatar-container">
    <img src="../../../assets/img/icon_bear.png"/>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

export default defineComponent({
  name: "Avatar",
  setup() {
    const store = useGameStore()

    return {}
  }
})
</script>

<style scoped lang="scss">

.avatar-container {
  position: absolute;
  left: 10px;
  top: 10px;
  pointer-events: auto;
  border: 4px solid rgba(70, 48, 31, 0.75);
  border-radius: 3px;
}

img {
  float: left;
}
</style>
