<template>
  <div>

    <form class="chat-form"
          @submit.prevent="chatSubmit"
          action="#">
      <div>
        <ul>
          <li v-for="r in store.chatHistory">
            <span class="chat-line">{{ r.title }}: {{ r.text }}</span>
          </li>
        </ul>
      </div>
      <input class="text-input" autocomplete="off" type="text" v-model="chatText" id="inputChat"
             v-on:keyup.prevent="keyup">
      <input type="submit" value=">">
    </form>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import GameClient from "../../net/GameClient";
import {useGameStore} from "../../store/game";

export default defineComponent({
  name: "Chat",
  setup() {
    const store = useGameStore()
    const chatText = ref("")
    let chatHistoryIndex = 0

    const keyup = (e: KeyboardEvent) => {
      // navigate by chat history
      if (e.key == "ArrowDown") {
        if (store.chatHistory.length > 0 && chatHistoryIndex < store.chatHistory.length - 1) {
          chatHistoryIndex++
          chatText.value = store.chatHistory[chatHistoryIndex].text
        }
      } else if (e.key == "ArrowUp") {
        if (store.chatHistory.length > 0 && chatHistoryIndex > 0) {
          chatHistoryIndex--
          chatText.value = store.chatHistory[chatHistoryIndex].text
        }
      }
    }

    const chatSubmit = () => {
      if (chatText.value.length > 0) {
        console.log("chat submit", chatText.value)
        GameClient.remoteCall("chat", {
          text: chatText.value
        })
        chatText.value = ""
      }
    }

    return {store, chatText, keyup, chatSubmit}
  }
})
</script>

<style scoped lang="scss">

.chat-form {
  position: absolute;
  left: 20px;
  bottom: 20px;
  pointer-events: none;
}

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

.text-input {
  width: 300px;
  font-size: 20px;
  pointer-events: auto;
}


</style>
