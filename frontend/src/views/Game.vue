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

    console.log("selectedCharacterId=" + Client.instance.selectedCharacterId)

    Net.instance.onDisconnect = () => {
      console.log("onDisconnect")
      router.push({name: 'Characters'})
    }
    Net.instance.onConnect = () => {
      Net.remoteCall("test", {n: 1, t: "err"}).then(d => {
        console.log("RECV game call")
        console.log(d)
      });
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