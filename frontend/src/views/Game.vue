<template>
  <div></div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import router from "@/router";
import Client from "@/net/Client";
import Game from "@/game/Game";

export default defineComponent({
  name: "Game",
  mounted() {
    Client.instance.clear()
    Net.instance = new Net(Client.wsUrl)

    console.log("selectedCharacterId=" + Client.instance.selectedCharacterId)

    Net.instance.onDisconnect = () => {
      console.log("onDisconnect")
      Client.instance.clear()
      Game.stop()
      router.push({name: 'Characters'})
    }
    Net.instance.onConnect = () => {
      // авторизуемся на игровом сервере, создаем игровую сессию
      // входим в мир выбранным персонажем
      Net.remoteCall("ssid", {
        ssid: Client.instance.ssid,
        selectedCharacterId: Client.instance.selectedCharacterId
      })
          .then(r => {
            console.log(r)
            Game.start();
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