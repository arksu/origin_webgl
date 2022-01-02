<template>
  <div class="container">
    <div class="row" v-bind:class="{new_char: id === 0, bg_deleting : deleteInProcess, bg_selecting: selectInProcess }"
         @click.prevent="selectChar">
      {{ name }} {{ id !== 0 ? "[id " + id + "]" : "" }}
    </div>

    <div v-if="id !== 0" class="row delete-char" v-bind:class="{bg_selecting: selectInProcess}"
         @click.prevent="deleteChar">
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
import {defineComponent, onMounted} from 'vue'
import {useApi} from "../../composition/useApi";
import router from "../../router";
import {RouteNames} from "../../router/routeNames";

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
    },
    // "занят" ли чар (происходит вход в мир каким то персонажем), нельзя выбрать перса сейчас
    busy: {
      type: Boolean,
      required: true
    }
  },
  emits: ['onDeleted', 'onSelect', 'onEnter'],
  setup(props, {emit}) {

    const {isLoading: deleteInProcess, fetch: fetchDelete} = useApi("characters/" + props.id, {
      method: 'DELETE',
    })

    const {isLoading: selectInProcess, data, isSuccess, fetch: fetchSelect} = useApi("characters/select/" + props.id, {
      method: 'POST',
    })

    const selectChar = async () => {
      if (props.id == 0) {
        await router.replace({name: RouteNames.NEW_CHARACTER})
      } else {
        if (props.busy) return
        // шлем в родителя события выбора чара, чтобы он заблокировал все остальнеы строки
        emit('onSelect')
        await fetchSelect()
        if (isSuccess.value) {
          const token = data.value.token
          localStorage.setItem('lastSelectedChar', "" + props.id)
          console.log("token for game", token)
          await router.push({name: RouteNames.GAME, params: {token}})
        }
      }
    }

    const deleteChar = async () => {
      // не даем удалять строку которая создает нового чара
      if (props.id == 0) return;

      // if (!confirm("Are you sure to delete this character: " + props.name)) return;
      if (deleteInProcess.value) return

      await fetchDelete()

      // пошлем в родителя эвент, чтобы он удалил из списка эту строку
      emit('onDeleted', props.id)
    }

    onMounted(() => {
      // реализуем автовход последним выбранным персонажем если установлен режим разработчика
      if (localStorage.getItem("dev") === "1") {
        const last = localStorage.getItem("lastSelectedChar")
        if (last) {
          selectChar()
        }
      }
    })

    return {selectChar, deleteChar, deleteInProcess, selectInProcess}
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

.row:hover:not(.bg_selecting):not(.new_char) {
  transition-duration: 0.6s;
  background: #228CBEFF;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.7);
  animation: btn-glow 0.6s ease-in-out infinite alternate;
}

.new_char {
  background-color: rgba(35, 93, 41, 0.6);
}

.new_char:hover {
  transition-duration: 0.6s;
  background: #4A9854FF;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.7);
  animation: btn-glow 0.6s ease-in-out infinite alternate;
}

.delete-char {
  width: 12%;
  background-color: rgba(174, 35, 39, 0.6);
}

.delete-char:hover {
  background: rgba(220, 41, 48, 0.6) !important;
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

.bg_deleting {
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

.bg_selecting {
  color: #7ec7d0;
  background: repeating-linear-gradient(
          -45deg,
          #548f8f,
          #548f8f 10px,
          #4b7d83 10px,
          #4b7d83 20px
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
