import {Core} from "./Core";

export class Net {

    private _core: Core;

    constructor(core: Core) {
        this._core = core;
    }

    public start() {
        let socket = new WebSocket("ws://localhost:8080");
        console.log(socket);
        socket.binaryType = "arraybuffer";

        let data = {
            i: 12,
            s: "foo",
            m: {
                a: 1,
                b: "33"
            },
            arr: [4, 5, 7]
        };

        socket.onopen = () => {
            console.log("ws connected");

            setTimeout(() => {
                let s = JSON.stringify(data);
                let buffer = new Buffer(s, 'utf8');
                socket.send(buffer);
            }, 1000);

        };

    }
}