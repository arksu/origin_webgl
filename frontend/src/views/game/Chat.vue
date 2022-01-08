<template>
  <div class="chat">
    <div class="rows">
      <ul>
        <li v-for="r in store.chatLines">
          <span class="chat-line">{{ r.title }}: {{ r.text }}</span>
        </li>
      </ul>
    </div>

    <form class="chat-form"
          @submit.prevent="chatSubmit"
          action="#">
      <div class="input-container">
        <input class="text-input" placeholder="Chat here" autocomplete="off" type="text" v-model="chatText"
               id="inputChat"
               v-on:keyup.prevent="keyup">
        <span class="submit-logo" @click="chatSubmit">
          <input type="submit" value="&#xf1d8" class="far fa-paper-plane fa-lg">
        </span>
      </div>
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
    // индекс в истории от конца
    let chatHistoryIndex = -1

    const keyup = (e: KeyboardEvent) => {
      // navigate by chat history
      const length = store.chatHistory.length;
      if (e.key == "ArrowUp") {
        if (length > 0 && chatHistoryIndex < length - 1) {
          chatHistoryIndex++
          chatText.value = store.chatHistory[length - chatHistoryIndex - 1]
        }
      } else if (e.key == "ArrowDown") {
        if (length > 0 && chatHistoryIndex > 0) {
          chatHistoryIndex--
          chatText.value = store.chatHistory[length - chatHistoryIndex - 1]
        }
      }
    }

    const chatSubmit = () => {
      if (chatText.value.length > 0) {
        console.log("chat submit", chatText.value)
        GameClient.remoteCall("chat", {
          text: chatText.value
        })
        store.chatHistory.push(chatText.value)
        chatText.value = ""
        chatHistoryIndex = -1
      }
    }

    return {store, chatText, keyup, chatSubmit}
  }
})
</script>

<style scoped lang="scss">

.chat {
  position: absolute;
  left: 10px;
  bottom: 10px;
  width: calc(100% - 70px);
  max-width: 340px;
}

.rows {
  pointer-events: none;
  font-family: Bitter, Georgia, serif;
  font-size: 20px;
  color: #daedfa;
  text-shadow: 2px 2px 0 #000,
  -1px -1px 0 #000,
  1px -1px 0 #000,
  -1px 1px 0 #000,
  1px 1px 0 #000;
}

.input-container {
  border: 2px solid #103c2ab5;
  border-radius: 6px;
  background-color: #7b917eb3;
  padding: 0.3em 0.8em;
  pointer-events: auto;
  //width: 100%;
  white-space: nowrap;
  display: flex;
  justify-content: space-between;
}

.chat-form {
  pointer-events: none;
}

.text-input {
  background: transparent;
  outline: none;
  border: none;
  width: 100%;
  font-size: 16px;
  pointer-events: auto;
  color: #17241d;
}

.text-input::placeholder {
  color: #446755;
}

.submit-logo {
  //width: 15%;
  color: #264a44;
  cursor: pointer;
}

</style>
