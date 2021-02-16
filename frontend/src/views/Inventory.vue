<template>
  <div ref="draggableHeader" class="draggable" @touchstart.prevent="onTouchStart" @mousedown.prevent="onMouseDown">
    <span>
      Title
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Inventory",
  data() {
    return {
      clientX: 0 as number,
      clientY: 0 as number,
      movementX: 0 as number,
      movementY: 0 as number,
      touchId: -1 as number
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

        const el = <HTMLDivElement>this.$refs.draggableHeader
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

      this.clientX = event.clientX
      this.clientY = event.clientY
      document.onmousemove = this.onDrag
      document.onmouseup = this.onDragEnd
    },
    onDrag: function (event: MouseEvent) {
      event.preventDefault()
      this.movementX = this.clientX - event.clientX
      this.movementY = this.clientY - event.clientY
      this.clientX = event.clientX
      this.clientY = event.clientY

      const el = <HTMLDivElement>this.$refs.draggableHeader
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

<style>

.draggable {
  position: absolute;
  left: 100px;
  top: 200px;
  width: 100px;
  height: 100px;
  z-index: 100;
  background-color: white;
  border: 1px solid #232323;
  border-radius: 5px;
  padding: 10px;
}

</style>