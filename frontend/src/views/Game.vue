<template>
  <div>GAME</div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import NetV2 from "@/net/NetV2";
import router from "@/router";
import Client from "@/net/Client";

export default defineComponent({
  name: "Game",
  mounted() {
    NetV2.instance = new NetV2(Client.wsUrl)

    NetV2.instance.onDisconnect = function () {
      router.push({name: 'Characters'})
      console.log("onDisconnect")
    }
  }
});

</script>


<style scoped>

</style>