<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <form v-if="active" style="position: absolute; z-index: 10; left: 20px; bottom: 20px" @submit.prevent="submit"
        action="#">
    <div>
      <li v-for="r in chatRows">
        <span>{{ r }}</span>
      </li>
      <!-- hack for refresh chatRows value -->
      <span style="display: none">{{ cnt }}</span>
    </div>
    <input style="width: 300px; font-size: 20px" type="text" v-model="chatText" id="inputChat">
    <input type="submit" value=">">
  </form>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import router from "@/router";
import Client from "@/net/Client";
import Game from "@/game/Game";

export default defineComponent({
  name: "Game",
  data() {
    return {
      active: false as boolean,
      chatText: "" as string,
      chatRows: [] as string[],
      cnt: 0 as number
    }
  },
  mounted() {
    Client.instance.clear()
    Net.instance = new Net(Client.wsUrl)

    console.log("selectedCharacterId=" + Client.instance.selectedCharacterId)

    Net.instance.onDisconnect = () => {
      console.log("onDisconnect")
      this.active = false
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
            this.active = true
            Game.start();
          })
    }

    Client.instance.onChatMessage = () => {
      this.chatRows = Client.instance.chatHistory.reverse()
      this.cnt = this.cnt + 1
      console.log(this.chatRows)
    }
  },
  unmounted() {
    if (Net.instance) Net.instance.disconnect();
  },
  computed: {
    // chatRows: function () {
    //   return Client.instance.chatHistory
    // }
  },
  methods: {
    submit() {
      console.log("submit " + this.chatText)
      Net.remoteCall("chat", {
        text: this.chatText
      })
      this.chatText = ""
    }
  }
});

</script>


<style scoped>

</style>