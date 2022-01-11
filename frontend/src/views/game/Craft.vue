<template>
  <window
      v-if="visible"
      :id="33"
      title="Craft"
      :inner-width="300"
      :inner-height="200"
      @close="$emit('close')"
  >
    <div class="flex-v">
      <!--    tabs-->
      <div class="tabs">
        <div class="tab">All</div>
        <div class="tab">Favorites</div>
        <div class="tab">History</div>
      </div>

      <!--    search-->
      <div class="search">
        <input type="text" placeholder="Search">
      </div>

      <div class="main-frame">
        <!--    list-->
        <div class="list">
          <div class="item"
               :class="{active: c.name === selected?.name}"
               v-for="c in store.craft.list"
               :key="c.name"
               @click="selectItem(c)">
            {{ convertCase(c.name) }}
          </div>
        </div>

        <!--    description, title, required, etc-->
        <div class="main-content">
          <div v-if="selected !== undefined">
            {{ convertCase(selected.name) }}
            <br>
            required:
            <div class="required">
              <div v-for="p in selected.required">
                <img alt="icon" :src="'/assets/game'+p.icon">
                {{ p.count }}
              </div>
            </div>
            produced:
            <div class="produced">
              <div v-for="p in selected.produced">
                <img alt="icon" :src="'/assets/game'+p.icon">
                {{ p.count }}
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
    <!--    buttons-->
    <div class="buttons-frame">
      <div class="button">Craft</div>
      <div class="button">Craft All</div>
    </div>
  </window>
</template>

<script lang="ts">
import Window from "./Window.vue";

import {defineComponent, ref} from "vue";
import {useGameStore} from "../../store/game";
import {CraftData} from "../../net/packets";

export default defineComponent({
  name: "Craft",
  components: {Window},
  emits: ['close'],
  props: {
    visible: Boolean
  },
  setup() {
    const store = useGameStore()
    const selected = ref<CraftData | undefined>(undefined)

    const selectItem = (i: any) => {
      selected.value = i
    }

    const convertCase = (s: string) => {
      return s.toLowerCase().replace("_", " ")
    }

    return {store, selectItem, selected, convertCase}
  }
})
</script>

<style scoped lang="scss">

$borderColor: #17241d;

.flex-v {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tabs {
  display: flex;
  justify-content: left;
  flex-wrap: wrap;
}

.tab {
  background-color: #506e42;
  padding: 0.1em;
}

.tab:hover {
  filter: brightness(110%);
}

.search {
  left: 0;
  width: 100px;
  margin-top: 5px;
}

.search input {
  border: $borderColor 1px solid;
  border-radius: 4px;
  background: transparent;
  padding: 0 0.3em 0 0.3em;
  color: #c3e8e8;
}

.search input::placeholder {
  color: #477359;
}

.main-frame {
  margin-top: 5px;
  flex-grow: 1;
  display: flex;
  justify-content: left;
  align-items: stretch;
  min-height: 0;
}

.list {
  background-color: rgba(15, 23, 19, 0.18);
  border: $borderColor 1px solid;
  border-radius: 4px;
  width: 100px;
  box-sizing: border-box;

  display: flex;
  align-items: stretch;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  min-height: 0;
}

.item {
  font-size: 14px;
  text-align: left;
  color: #7f99a6;
}

.item.active {
  background-color: rgb(67, 100, 39);
  color: #c3e8e8;
}

.item:hover {
  background-color: rgb(67, 100, 39);
}

.main-content {
  text-align: left;
  padding-left: 10px;
  flex-grow: 1;
}

.required, .produced  {
  display: flex;
  flex-direction: row;
}

.buttons-frame {
  position: absolute;
  padding-right: inherit;
  padding-bottom: inherit;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: right;
}

.button {
  background-color: #4b7d83;
  padding: 0.1em;
}

.button:hover {
  filter: brightness(110%);
}
</style>