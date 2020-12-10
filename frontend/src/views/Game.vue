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
      Net.remoteCall("ssid", {
        ssid: Client.instance.ssid
      });

      Net.remoteCall("gameEnter", {
        selectedCharacterId: Client.instance.selectedCharacterId
      }).then(d => {

      })
    }
  },
  unmounted() {
    Net.instance?.disconnect();
  }
});

</script>


<style scoped>

</style>