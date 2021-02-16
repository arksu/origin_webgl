<template>
  <div ref="draggableTarget" class="container">
    <div class="frame" @touchstart.prevent="onTouchStart" @mousedown.prevent="onMouseDown">
      <div v-for="(line, rows) in items">
        <div class="item-back" v-for="(item, cols) in line">
          <Item :title="item" :x="16 + cols * 35" :y="22 + rows * 35"></Item>
        </div>
      </div>
    </div>
    <div class="header">
      <div class="title">
        <span class="title-text">
          Inventory
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Item from "@/views/Item.vue";

export default defineComponent({
  name: "Inventory",
  components: {Item},
  data() {
    return {
      clientX: 0 as number,
      clientY: 0 as number,
      movementX: 0 as number,
      movementY: 0 as number,
      touchId: -1 as number,
      items: [['s', 'd', 'e', 'w'], ['f', 'g', 't', '4'], ['s', 'd', 'e', 'w'], ['f', 'g', 't', '4']]
    }
  },
  methods: {
    onTouchStart: function (event: TouchEvent) {
      if (event.touches.length == 1) {
        this.clientX = event.touches[0].clientX
        this.clientY = event.touches[0].clientY
        this.touchId = event.touches[0].identifier
        document.ontouchmove = this.onTouchDrag
        document.ontouchend = this.onTouchDragEnd
      }
    },
    onTouchDrag: function (event: TouchEvent) {
      event.preventDefault()
      if (event.touches.length == 1 && event.touches[0].identifier == this.touchId) {

        this.movementX = this.clientX - event.touches[0].clientX
        this.movementY = this.clientY - event.touches[0].clientY
        this.clientX = event.touches[0].clientX
        this.clientY = event.touches[0].clientY

        const el = <HTMLDivElement>this.$refs.draggableTarget
        el.style.left = (el.offsetLeft - this.movementX) + 'px'
        el.style.top = (el.offsetTop - this.movementY) + 'px'
      }
    },
    onTouchDragEnd: function (event: TouchEvent) {
      console.log(event)
      document.ontouchmove = null
      document.ontouchend = null
    },
    onMouseDown: function (event: MouseEvent) {
      if (event.button == 0) {
        this.clientX = event.clientX
        this.clientY = event.clientY
        document.onmousemove = this.onDrag
        document.onmouseup = this.onDragEnd
      }
    },
    onDrag: function (event: MouseEvent) {
      event.preventDefault()
      this.movementX = this.clientX - event.clientX
      this.movementY = this.clientY - event.clientY
      this.clientX = event.clientX
      this.clientY = event.clientY

      const el = <HTMLDivElement>this.$refs.draggableTarget
      el.style.left = (el.offsetLeft - this.movementX) + 'px'
      el.style.top = (el.offsetTop - this.movementY) + 'px'
    },
    onDragEnd: function () {
      document.onmousemove = null
      document.onmouseup = null
    }
  }
})
</script>

<style scoped>

.container {
  position: absolute;
  left: 100px;
  top: 200px;
  width: 173px;
  height: 178px;
  z-index: 100;
}

.header {
  position: absolute;
  width: 100%;
}

.title {
  border-left: 18px solid transparent;
  border-right: 18px solid transparent;
  border-top: 0px solid transparent;
  border-bottom: 0px solid transparent;
  border-image: url('/assets/window_title.png') 0 30% 0 30% fill / 0 18px 0 18px;
  position: relative;
  height: 22px;
  display: inline-block;
}

.title-text {
  color: #eeee59;
  font-size: 14px;
  font-family: Georgia, serif;
}

.frame {
  position: absolute;
  top: 7px;
  width: 100%;
  height: 100%;
  border-image: url('/assets/window_frame.png') 34% fill / 8px repeat repeat;
}
</style>