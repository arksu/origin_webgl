export class Net {

    public start() {
        let socket = new WebSocket("ws://localhost:8080");
        console.log(socket);

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
                socket.send(s);
            }, 1000);

        };
    }

    public login(login: string, password: string): void {

    }
}