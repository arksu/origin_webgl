<template>
  <div class="container">
    <h1>Page: {{ $route.params.catchAll }}</h1>
    <h1>Oops, it looks like the page you're looking for doesn't exist.</h1>
  </div>
</template>

<script>
export default {
  name: "NotFound"
}
</script>

<style lang="scss">

.container {
  text-align: center;
}

</style>