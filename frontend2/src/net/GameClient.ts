// websockets для работы игрового протокола
const websocketsUrl = ("https:" === window.location.protocol ? "wss:" : "ws:") + "//" + window.location.hostname + ":" + window.location.port + "/api/game";

enum State {
    Disconnected,
    Connecting,
    Connected,
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
}

interface Response {
    id: number;

    /**
     * error if request is not success
     */
    e?: any;

    /**
     * channel
     */
    c?: string;

    /**
     * data
     */
    d: any;
}

export default class GameClient {
    public static instance?: GameClient

    /**
     * ссылка для коннекта к вебсокет серверу
     * @private
     */
    private static readonly url: string = ("https:" === window.location.protocol ? "wss:" : "ws:") + "//"
        + window.location.hostname + ":"
        + window.location.port
        + "/api/game";

    /**
     * сокет
     */
    private socket?: WebSocket = undefined;

    /**
     * состояние клиента сети
     * @private
     */
    private state: State = State.Disconnected;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     */
    private requests: { [id: number]: Request } = {};

    /**
     * каждый запрос имеет свой id, увеличиваем его на 1 при каждом запросе
     */
    private lastId: number = 0;

    /**
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    public onDisconnect?: () => void;
    public onServerError?: (m: string) => void
    public onConnect?: () => void;

    public static createNew(): GameClient {
        if (GameClient.instance !== undefined) {
            GameClient.instance.disconnect()
        }
        GameClient.instance = new GameClient();
        return GameClient.instance
    }

    private constructor() {
        this.connect()
    }

    /**
     * подключиться
     */
    private connect() {
        // сеть должна быть не активна для старта
        if (this.state !== State.Disconnected) {
            console.error("ws connect [" + this.state + "] wrong state");
            return;
        }

        this.createSocket();
    }

    public disconnect() {
        this.state = State.Disconnected;
        this.socket?.close();
        this.socket = undefined;
        GameClient.instance = undefined
    }

    /**
     * создать вебсокет
     * @private
     */
    private createSocket() {
        console.warn("ws connecting...");
        this.state = State.Connecting;
        this.socket = new WebSocket(GameClient.url);

        this.socket.onopen = this.onopen.bind(this);
        this.socket.onclose = this.onclose.bind(this);
        this.socket.onmessage = this.onmessage.bind(this);
    }

    /**
     * обработка открытия сокета
     * @param {Event} _ev
     */
    private onopen(_ev: Event) {
        console.warn("ws connected");

        if (this.state !== State.Connecting) {
            throw Error("wrong state websocket on open");
        }

        this.state = State.Connected;
        if (this.onConnect !== undefined) {
            this.onConnect();
        }
    }

    /**
     * обработка закрытия сокета
     * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
     * @param {CloseEvent} ev
     */
    private onclose(ev: CloseEvent) {
        console.warn("ws closed [" + ev.code + "] " + ev.reason);

        this.state = State.Disconnected;
        this.socket = undefined;

        if (ev.code == 1011 && this.onServerError !== undefined) {
            this.onServerError(ev.reason)
        } else if (this.onDisconnect !== undefined) {
            this.onDisconnect();
        }
    }

    /**
     * получение сообщений от сервера в сокете
     * @param {MessageEvent} ev
     */
    private onmessage(ev: MessageEvent) {
        if (typeof ev.data === "string") {
            let response: Response = JSON.parse(ev.data);
            // console.log("%cRECV", 'color: #1BAC19', _.cloneDeep(response));

            // пришло сообщение в общий канал (не ответ на запрос серверу)
            if (response.id === 0 && response.c !== undefined) {
                this.onChannelMessage(response.c, response.d);
            } else {
                // ищем в мапе по ид запроса
                let request: Request = this.requests[response.id];
                if (request !== undefined) {
                    // удалим из мапы
                    delete this.requests[response.id];

                    // ответ успешен?
                    if (response.e === undefined) {
                        request.resolve(response.d);
                    } else {
                        console.error(response.e);
                        // иначе была ошибка, прокинем ее
                        request.reject(response.e);
                    }
                }
            }
        } else {
            console.warn("unknown data type: " + (typeof ev.data));
            console.warn("RECV", ev.data)
        }
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} channel канал данных
     * @param data
     */
    protected onChannelMessage(channel: string, data: any) {

    }
}