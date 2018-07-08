export class Net {

    constructor() {
    }

    public start() {
        let socket = new WebSocket("ws://localhost:8080");
        console.log(socket);

        socket.onopen = function () {
            console.log("ws connected");
        };
    }
}