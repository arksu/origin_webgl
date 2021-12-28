<template>
  <div>

    <form class="chat-form"
          @submit.prevent="chatSubmit"
          action="#">
      <div>
        <ul>
          <li v-for="r in chatRows">
            <span class="chat-line">{{ r }}</span>
          </li>
        </ul>
        <!-- hack for refresh chatRows value -->
        <span style="display: none">{{ cnt }}</span>
      </div>
      <input class="text-input" type="text" v-model="chatText" id="inputChat"
             v-on:keyup.prevent="keyup">
      <input type="submit" value=">">
    </form>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'

export default defineComponent({
  name: "Chat",
  setup() {
    const chatRows = ref([])
    const cnt = ref(4)
    const chatText = ref("")

    const keyup = () => {

    }

    const chatSubmit = () => {

    }

    return {chatRows, cnt, chatText, keyup, chatSubmit}
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
