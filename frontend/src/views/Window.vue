<template>
  <div ref="draggableTarget" class="container" :style="windowStyle">
    <div class="frame">
      <slot></slot>
    </div>

    <div class="close-btn-back">
      <img style="float: right; margin-right: 3px" src="assets/window_close.png">
    </div>
    <div class="header" @touchstart.prevent="onTouchStart" @mousedown.prevent="onMouseDown">
      <div class="title">
        <span class="title-text">
          {{ title }}
        </span>
      </div>
    </div>
    <div class="close-btn-back2">
      <div class="close-btn" @click="$emit('close')"></div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Window",
  props: {
    title: String,
    width: Number,
    height: Number
  },
  emits: {
    close: null
  },
  data() {
    return {
      clientX: 0 as number,
      clientY: 0 as number,
      movementX: 0 as number,
      movementY: 0 as number,
      touchId: -1 as number,
    }
  },
  computed: {
    windowStyle(): string {
      return "width: " + this.width + "px; height: " + this.height + "px;"
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
      console.log(event)
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

<style scoped lang="scss">
.container {
  display: flex;
  justify-content: right;
  position: absolute;
  left: 100px;
  top: 200px;
  z-index: 100;
  text-align: center;
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

.header {
  position: absolute;
  width: 100%;
  height: 25px;
  cursor: move;
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

.close-btn-back {
  position: absolute;
  top: 10px;
  width: 100%;
}

.close-btn-back2 {
  text-align: right;
  position: relative;
  width: 20%;
  height: 15px;
  margin-left: auto;
  top: 10px;
}

.close-btn {
  margin-left: auto;
  float: right;
  margin-right: 3px;
  width: 13px;
  height: 13px;
  background: transparent no-repeat;
  cursor: pointer;
  background-image: url('/assets/btn_close_hover.png');
}

.close-btn:hover {
  background-image: url('/assets/btn_close_hover.png');
}
</style>