<script lang="ts">
import Window from './Window.vue'

import { defineComponent, ref } from 'vue'
import GameClient from '../../net/GameClient'
import { useGameStore } from '@/stores/gameStore'
import { ClientPacket, type CraftData } from '@/net/packets'

export default defineComponent({
  name: 'Craft',
  components: { Window },
  // emits: ['close'],
  setup() {
    const store = useGameStore()
    const selected = ref<CraftData | undefined>(undefined)

    const selectItem = (i: any) => {
      selected.value = i
    }

    const convertCase = (s: string) => {
      s = s.toLowerCase().replace('_', ' ')
      return s.charAt(0).toUpperCase() + s.slice(1)
    }
    const craft = () => {
      if (selected.value !== undefined) {
        GameClient.instance?.send(ClientPacket.CRAFT, {
          name: selected.value.name,
          all: false
        })
      }
    }
    const craftAll = () => {
      if (selected.value !== undefined) {
        GameClient.instance?.send(ClientPacket.CRAFT, {
          name: selected.value.name,
          all: true
        })
      }
    }

    return { store, selectItem, selected, convertCase, craft, craftAll }
  }
})
</script>

<template>
  <window
    :id="33"
    :inner-height="200"
    :inner-width="300"
    title="Craft"
    @close="$emit('close')"
  >
    <div class="flex-v">
      <!--    tabs-->
      <div class="tabs">
        <div class="tab active">All</div>
<!--        <div class="tab">Favorites</div>-->
<!--        <div class="tab">History</div>-->
      </div>

      <!--    search-->
<!--      <div class="search">-->
<!--        <input placeholder="Search" type="text">-->
<!--      </div>-->

      <div class="main-frame">
        <!--    list-->
        <div class="list">
          <div v-for="c in store.craft.list"
               :key="c.name"
               :class="{active: c.name === selected?.name}"
               class="item"
               @click="selectItem(c)">
            <img :src="'/assets/game' + c.produced[0].icon" alt="">
            <span>{{ convertCase(c.name) }}</span>
          </div>
        </div>

        <!--    description, title, required, etc-->
        <div class="main-content">
          <div v-if="selected !== undefined">
            {{ convertCase(selected.name) }}
            <br>
            <div class="required">
              input:
              <div v-for="p in selected.required" :key="p.icon" class="craft-img">
                <img :src="'/assets/game'+p.icon" alt="">
                <span>{{ p.count }}</span>
              </div>
            </div>
            <div class="produced">
              result:
              <div v-for="p in selected.produced" :key="p.icon" class="craft-img">
                <img :src="'/assets/game'+p.icon" alt="">
                <span>{{ p.count }}</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
    <!--    buttons-->
    <div class="buttons-frame">
      <div class="button" @click="craft">Craft</div>
<!--      <div class="button" @click="craftAll">Craft All</div>-->
    </div>
  </window>
</template>

<style lang="scss" scoped>

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
  border: #133236 solid 2px;
  border-bottom: none;
  border-radius: 4px;
  margin: 0 1px;
}

.tab.active {
  color: #c3e8e8;
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

.search input:focus {
  outline: none;
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
  width: 140px;

  display: flex;
  align-items: stretch;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  min-height: 0;
}

.item {
  font-size: 13px;
  text-align: left;
  color: #7f99a6;
  white-space: nowrap;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.item.active {
  background-color: rgb(67, 100, 39);
  color: #c3e8e8;
}

.item:hover {
  background-color: rgb(67, 100, 39);
}

.item img {
  width: 25px;
  height: 25px;
  object-fit: contain;
}

.item span {
  padding: 0 0.2em;
}

.main-content {
  text-align: left;
  padding-left: 10px;
  flex-grow: 1;
  color: #7f99a6;
}

.required, .produced {
  display: flex;
  flex-direction: row;
}

.craft-img {
  width: 32px;
  height: 32px;
  position: relative;
  margin-right: 1px;
}

.craft-img img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.craft-img span {
  position: absolute;
  right: 0;
  bottom: 0;
  line-height: 100%;
  font-size: 12px;
  text-shadow: 1px 0 1px #000,
  0 1px 1px #000,
  -1px 0 1px #000,
  0 -1px 1px #000;
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
  padding: 0 0.2em;
  border: #133236 solid 2px;
  border-radius: 4px;
  margin-left: 3px;
}

.button:hover {
  filter: brightness(110%);
}
</style>