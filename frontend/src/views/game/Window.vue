<template>
  <div ref="draggableTarget" class="container"
       :style="'width: ' + width + 'px; height: ' + height + 'px; left: '+left +'px; top: '+top+'px;'">
    <div class="frame" :style="'width: ' + width + 'px; height: ' + height + 'px;'">
      <slot></slot>
    </div>

    <div class="close-btn-back">
      <img alt="" style="float: right; margin-right: 3px" src="../../../assets/window_close.png">
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
    id: Number, // id окна по которому восстанавливаем/сохраняем размеры
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
      left: 0 as number,
      top: 0 as number
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
        this.left = (el.offsetLeft - this.movementX)
        this.top = (el.offsetTop - this.movementY)
      }
    },
    onTouchDragEnd: function (event: TouchEvent) {
      console.log(event)
      document.ontouchmove = null
      document.ontouchend = null
      localStorage.setItem("wnd_" + this.id + "_left", "" + this.left)
      localStorage.setItem("wnd_" + this.id + "_top", "" + this.top)
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
      this.left = (el.offsetLeft - this.movementX)
      this.top = (el.offsetTop - this.movementY)
    },
    onDragEnd: function () {
      document.onmousemove = null
      document.onmouseup = null
      localStorage.setItem("wnd_" + this.id + "_left", "" + this.left)
      localStorage.setItem("wnd_" + this.id + "_top", "" + this.top)
    }
  },
  mounted() {
    this.left = parseInt(localStorage.getItem("wnd_" + this.id + "_left") ?? "100")
    this.top = parseInt(localStorage.getItem("wnd_" + this.id + "_top") ?? "150")
    if (this.left + this.width! > window.innerWidth || this.top + this.height! > window.innerHeight) {
      this.left = 100
      this.top = 150
    }
  }
})
</script>

<style scoped lang="scss">
.container {
  display: flex;
  justify-content: right;
  position: absolute;
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
  border-top: 0 solid transparent;
  border-bottom: 0 solid transparent;
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
  border-width: 0;
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
}

.close-btn:hover {
  background-image: url('/assets/btn_close_hover.png');
}
</style>
