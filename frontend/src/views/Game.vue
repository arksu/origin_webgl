<template>
  <div>GAME</div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import router from "@/router";
import Client from "@/net/Client";

export default defineComponent({
  name: "Game",
  mounted() {
    Net.instance = new Net(Client.wsUrl)

    Net.instance.onDisconnect = function () {
      router.push({name: 'Characters'})
      console.log("onDisconnect")
    }
  },
  unmounted() {
    Net.instance?.disconnect();
    Net.instance = undefined;


  }
});

</script>


<style scoped>

</style>