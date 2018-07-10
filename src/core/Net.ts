import * as MsgPack from "msgpack-lite";

export class Net {
    private bin: Buffer;

    constructor() {
        let a = {
            i: 334,
            arr: [0, 1, 2, 3],
            str: "string",
            sub: {
                some: 123,
                f: 4.5,
                s: "foo"
            }
        };
        this.bin = MsgPack.encode(a);
        // bin.
    }

    public start() {
        let socket = new WebSocket("ws://localhost:8080");
        console.log(socket);
        socket.binaryType = "arraybuffer";

        socket.onopen = () => {
            console.log("ws connected");

            // socket.send("1111");
            console.log(this.bin);

            setTimeout(() => {
                socket.send(this.bin);
            }, 1000);

        };

    }
}