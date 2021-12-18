<template>
  <div class="container">
    <div class="row" v-bind:class="{new_char: id === 0, bg : deleteInProcess }" @click.prevent="selectChar">
      {{ name }} {{ id !== 0 ? "[id " + id + "]" : "" }}
    </div>

    <div v-if="id !== 0" class="row delete-char" @click.prevent="deleteChar">
      <div v-if="deleteInProcess">
        <i class="fas fa-sync fa-spin"></i>
      </div>
      <div v-else>
        <i class="fas fa-trash-alt"></i>
      </div>
    </div>

  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useApi} from "../../composition/useApi";

export default defineComponent({
  name: 'CharacterRow',
  props: {
    name: {
      type: String,
      required: true
    },
    // id==0 empty row for create new character
    id: {
      type: Number,
      required: true
    }
  },
  setup(props) {
    if (props.id == 0) return;

    const {isLoading: deleteInProcess, fetch} = useApi("characters/" + props.id, {
      method: 'DELETE',
      skip: true
    })

    const deleteChar = () => {
      // if (!confirm("Are you sure to delete this character: " + props.name)) return;
      if (deleteInProcess.value) return

      fetch()
    }

    const selectChar = () => {

    }

    return {deleteChar, deleteInProcess}
  }
})
</script>
<style scoped lang="scss">

.container {
  width: 100%;
  display: table;
  margin: 10px 0;
}

.row {
  display: table-cell;
  border-radius: 6px;
  border-color: rgba(30, 67, 91, 0.6);
  border-width: 1px;
  border-style: solid;
  margin: 10px;
  padding: 5px 0 5px;
  background-color: #105858AA;
  cursor: pointer;
  text-align: center;
  width: 80%;
}

.row:hover {
  -webkit-transition-duration: 0.5s;
  transition-duration: 0.6s;
  background: rgb(34, 140, 190);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.7);
  animation: btn-glow 0.6s ease-in-out infinite alternate;
}

.new_char {
  background-color: rgba(35, 93, 41, 0.6);
}

.delete-char {
  width: 12%;
  background-color: rgba(174, 35, 39, 0.6);
}

.delete-char:hover {
  background: rgba(210, 57, 63, 0.6);
}

.loading {
  height: auto;
  text-align: center;
  color: #4c3f2e;
  position: relative;
  overflow: hidden;
  padding: 1rem;
  margin: 3%;
  font-style: italic;
}

.bg {
  color: #c49e9e;
  background: repeating-linear-gradient(
          -45deg,
          #6c6161,
          #6c6161 10px,
          #625253 10px,
          #625253 20px
  );
  background-size: 400% 400%;
  animation: moving-back 12s linear infinite;
}

@keyframes moving-back {
  100% {
    background-position: 100% 100%;
  }
}

</style>