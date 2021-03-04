<template>
  <div v-if="!active" class="padding-all">
    <div class="login-panel">
      <div>LOADING...</div>
    </div>
  </div>

  <form v-if="active" style="position: absolute; z-index: 10; left: 20px; bottom: 20px; pointer-events: none;"
        @submit.prevent="chatSubmit"
        action="#">
    <div>
      <li v-for="r in chatRows">
        <span class="chat-line">{{ r }}</span>
      </li>
      <!-- hack for refresh chatRows value -->
      <span style="display: none">{{ cnt }}</span>
    </div>
    <input class="chat-input" type="text" v-model="chatText" id="inputChat"
           v-on:keyup.prevent="keyup">
    <input style="pointer-events: auto;" type="submit" value=">">
  </form>

  <Inventory :inv="i" v-for="i in $store.state.inventories" :key="i.id"></Inventory>
  <Hand v-if="$store.state.hand !== undefined" :ox="$store.state.hand.mx" :oy="$store.state.hand.my"></Hand>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import router from "@/router";
import Client from "@/net/Client";
import Game from "@/game/Game";
import Inventory from "@/views/Inventory.vue";
import Hand from "@/views/Hand.vue";
import {ActionTypes} from "@/store/action-types";

export default defineComponent({
  name: "Game",
  components: {Hand, Inventory},
  data() {
    return {
      active: false as boolean,
      chatText: "" as string,
      chatRows: [] as string[],
      cnt: 0 as number,
      chatHistory: [] as string[],
      chatHistoryIndex: 0 as number,
    }
  },
  mounted() {
    Client.instance.clear()
    Net.instance = new Net(Client.wsUrl)

    console.log("selectedCharacterId=" + Client.instance.selectedCharacterId)
    let historyStr = localStorage.getItem("chatHistory")
    if (historyStr !== null) {
      this.chatHistory = JSON.parse(historyStr)
      this.chatHistoryIndex = -1
    }

    Net.instance.onServerError = (m: string) => {
      console.warn("onServerError", m)
      this.active = false
      Game.stop()
      this.$store.dispatch(ActionTypes.NETWORK_ERROR, m);
    }
    Net.instance.onDisconnect = () => {
      console.warn("onDisconnect")
      this.active = false
      Game.stop()
      router.push({name: 'Characters'})
    }
    Net.instance.onConnect = () => {
      // авторизуемся на игровом сервере, создаем игровую сессию
      // входим в мир выбранным персонажем
      Net.remoteCall("ssid", {
        ssid: this.$store.getters.ssid!!,
        selectedCharacterId: Client.instance.selectedCharacterId
      })
          .then(r => {
            console.log(r)
            this.active = true
            Game.start();
          })
    }

    Client.instance.onChatMessage = () => {
      console.log(Client.instance.chatHistory)
      this.chatRows = [...Client.instance.chatHistory].reverse()
      this.cnt = this.cnt + 1
      console.log("chat:")
      console.log(this.chatRows)
    }
  },
  unmounted() {
    if (Net.instance) Net.instance.disconnect();
  },
  methods: {
    chatSubmit() {
      if (this.chatText !== undefined && this.chatText.length > 0) {
        console.log("chat submit " + this.chatText)
        this.chatHistory.unshift(this.chatText)
        this.chatHistory.splice(7)
        this.chatHistoryIndex = -1
        localStorage.setItem("chatHistory", JSON.stringify(this.chatHistory))
        Net.remoteCall("chat", {
          text: this.chatText
        })
        this.chatText = ""
      }
    },
    keyup(e: KeyboardEvent) {
      // navigate by chat history
      if (e.key == "ArrowUp") {
        if (this.chatHistory.length > 0 && this.chatHistoryIndex < this.chatHistory.length - 1) {
          this.chatHistoryIndex++
          this.chatText = this.chatHistory[this.chatHistoryIndex]
        }
      } else if (e.key == "ArrowDown") {
        if (this.chatHistory.length > 0 && this.chatHistoryIndex > 0) {
          this.chatHistoryIndex--
          this.chatText = this.chatHistory[this.chatHistoryIndex]
        }
      }
    }
  }
});

</script>


<style lang="scss">

.chat-line {
  pointer-events: none;
  font-family: Bitter, Georgia, serif;
  font-size: 25px;
  color: #daedfa;
  text-shadow: 2px 2px 0 #000,
  -1px -1px 0 #000,
  1px -1px 0 #000,
  -1px 1px 0 #000,
  1px 1px 0 #000;
}

.chat-input {
  width: 300px;
  font-size: 20px;
  pointer-events: auto;
}
</style>