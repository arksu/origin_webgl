<template>
  <div>
    <pre>
    {{ d }}
      </pre>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import sleep from "../utils/sleep";

const tt = async () => {
  await sleep(1000)

  const r = {
    some: 'asd'
  }
  return {r}
}

export default defineComponent({
  name: "Test",
  async setup() {
    const d = await tt()
    console.log(d)
    return {d}
  }
})</script>

<style scoped>

</style>