<template>
  <div ref="draggableHeader" class="draggable" @mousedown.prevent="onMouseDown">
    <span>
      Title
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
// import { ref, onMounted } from 'vue'

export default defineComponent({
  name: "Inventory",
  data() {
    return {
      clientX: 0 as number,
      clientY: 0 as number,
      movementX: 0 as number,
      movementY: 0 as number

    }
  },
  // setup() {
  //   const draggableHeader = ref(null)
  //   onMounted(() => {
  //     console.log(draggableHeader)
  //   })
  //   return {
  //     draggableHeader
  //   }
  // },
  methods: {
    onMouseDown: function (event: MouseEvent) {
      console.log(event)

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
}
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
  padding: 10px;
}

</style>