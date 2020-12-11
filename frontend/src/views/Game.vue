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
      // авторизуемся на игровом сервере, создаем игровую сессию
      // входим в мир выбранным персонажем
      Net.remoteCall("ssid", {
        ssid: Client.instance.ssid,
        selectedCharacterId: Client.instance.selectedCharacterId
      });
    }
  },
  unmounted() {
    Net.instance?.disconnect();
  }
});

</script>


<style scoped>

</style>