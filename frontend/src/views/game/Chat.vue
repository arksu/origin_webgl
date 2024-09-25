<script lang="ts">
import { defineComponent, ref } from 'vue'
import { useGameStore } from '@/stores/gameStore'
import { ClientPacket } from '@/net/packets'
import Render from '@/game/Render'

export default defineComponent({
  name: 'Chat',
  setup() {
    const store = useGameStore()
    const chatText = ref('')
    // индекс в истории от конца
    let chatHistoryIndex = -1

    const keyup = (e: KeyboardEvent) => {
      // navigate by chat history
      const length = store.chatHistory.length
      if (e.key == 'ArrowUp') {
        if (length > 0 && chatHistoryIndex < length - 1) {
          chatHistoryIndex++
          chatText.value = store.chatHistory[length - chatHistoryIndex - 1]
        }
      } else if (e.key == 'ArrowDown') {
        if (length > 0 && chatHistoryIndex > 0) {
          chatHistoryIndex--
          chatText.value = store.chatHistory[length - chatHistoryIndex - 1]
        }
      }
    }

    const chatSubmit = () => {
      if (chatText.value.length > 0) {
        console.log('chat submit', chatText.value)
        store.client!.send(ClientPacket.CHAT, {
          text: chatText.value
        })
        store.chatHistory.push(chatText.value)
        chatText.value = ''
        chatHistoryIndex = -1
      }
    }

    return { store, chatText, keyup, chatSubmit }
  }
})
</script>

<template>
  <div class="chat">
    <div class="rows">
      <ul>
        <li v-for="r in store.chatLines">
          <span class="chat-line">{{ r.title }}: {{ r.text }}</span>
        </li>
      </ul>
    </div>

    <form action="#"
          class="chat-form"
          @submit.prevent="chatSubmit">
      <div class="input-container">
        <input id="inputChat" v-model="chatText" autocomplete="off" class="text-input" placeholder="Chat here"
               type="text"
               v-on:keyup.prevent="keyup">
        <span class="submit-logo" @click="chatSubmit">
          <i class="fa-regular fa-paper-plane"></i>
        </span>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>

.chat {
  position: absolute;
  left: 4px;
  bottom: 4px;
  width: calc(100% - 70px);
  max-width: 340px;
}

.rows {
  pointer-events: none;
  //font-family: Bitter, Georgia, serif;
  font-size: 16px;
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
  font-size: large;
  color: #264a44;
  cursor: pointer;
}

</style>
