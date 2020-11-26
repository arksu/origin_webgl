<template>

</template>

<script>
export default {
  name: "Game"
}

/*
let proto = "https:" === window.location.protocol ? "wss" : "ws";
let net = new Net(proto + "://" + window.location.hostname + ":8010/ws");
console.log("Net url: " + net.url);

Net.instance = net;

net.onDisconnect = () => {
  console.log("net disconnected");
};


Net.instance.remoteCall("login", {
  login : this.login,
  password: this.password
})
    .then((d) => {
      console.log("successLogin")
    })
    .catch((e) => {
      console.error(e);

      let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
      loginBtn.disabled = false;

      console.log("showLoginError")
    });

 */
</script>


<style scoped>

</style>