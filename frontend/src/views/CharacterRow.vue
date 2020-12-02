<template>
  <div style="width: 100%; display: table; margin: 10px 0">
    <div :class="getClass()" :onclick="select">
      {{ name }} {{ id !== 0 ? "id (" + id + ")" : "" }}
    </div>
    <div v-if="id !== 0" class="character-row delete-char" :onclick="deleteChar">
      <i class="fas fa-trash-alt"></i>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import router from "@/router";

export default defineComponent({
  name: "CharacterRow",
  props: {
    id: Number,
    name: String
  },
  methods: {
    getClass: function (): string {
      return this.id == 0 ? "character-row new-char" : "id==0 ? character-row";
    },
    select(e: Event) {
      e.preventDefault()
      if (this.id == 0) {
        console.log("create new char")
        router.push({name: 'NewCharacter'})
      } else {
        console.log("selected " + this.name)
      }
    },
    deleteChar(e: Event) {
      e.preventDefault()

      console.log("delete " + this.id)
    }
  }
});
</script>

<style scoped>

</style>