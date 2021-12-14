import _ from "lodash";
import store from "@/store/store"
import Client from "@/net/Client";
import Game from "@/game/Game";
import MoveController from "@/game/MoveController";
import {
    ActionProgressData,
    ContextMenuData,
    HandData,
    InventoryUpdate,
    MapGridData,
    ObjectDel,
    ObjectMoved,
    ObjectStopped,
    StatusUpdate,
    ServerPacket
} from "@/net/Packets";
import {MutationTypes} from "@/store/mutation-types";

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

/**
 * Websocket реализация сети (используется только в самой игре)
 */
export default class Net {
    public static instance?: Net = undefined;

    /**
     * ссылка для коннекта к вебсокет серверу
     * @private
     */
    private readonly url: string;

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
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    public onDisconnect?: Callback;
    public onServerError?: (m: string) => void
    public onConnect?: Callback;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     */
    private requests: { [id: number]: Request } = {};

    /**
     * каждый запрос имеет свой id, увеличиваем его на 1 при каждом запросе
     */
    private lastId: number = 0;

    constructor(url: string) {
        this.url = url;
        this.connect();
    }

    /**
     * подключиться
     */
    private connect() {
        // есть должна быть не активна для старта
        if (this.state !== State.Disconnected) {
            console.log("ws connect [" + this.state + "] wrong state");
            return;
        }

        this.createSocket();
    }

    public disconnect() {
        this.state = State.Disconnected;
        this.socket?.close();
        this.socket = undefined;
        Net.instance = undefined
    }

    /**
     * создать вебсокет
     * @private
     */
    private createSocket() {
        console.warn("ws connecting...");
        this.state = State.Connecting;
        this.socket = new WebSocket(this.url);

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
            console.log("%cRECV", 'color: #1BAC19', _.cloneDeep(response));

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

    private socketSend(data: any): void {
        let d = JSON.stringify(data);
        console.log("%cSEND", 'color: red', _.cloneDeep(data));
        this.socket!.send(d);
    }

    public static async remoteCall(target: string, req?: any): Promise<any> {
        if (this.instance == undefined) {
            throw Error("net instance is undefined");
        }

        // формируем запрос на севрер
        let id = ++this.instance.lastId;
        let data = {
            id: id,
            t: target,
            d: req
        };

        return new Promise((resolve, reject) => {
            // отправляем данные в сокет
            this.instance!!.socketSend(data);

            // запишем запрос в мапу запросов
            // промис завершится когда придет ответ
            this.instance!!.requests[id] = {
                resolve,
                reject
            };
        });
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} channel канал данных
     * @param data
     */
    protected onChannelMessage(channel: string, data: any) {
        switch (channel) {
            case ServerPacket.MAP_DATA: {
                let p = (<MapGridData>data)
                let key = p.x + "_" + p.y;
                switch (p.a) {
                    case 0 : // delete
                        delete Client.instance.map[key]
                        Game.instance?.deleteGrid(p.x, p.y)
                        break
                    case 1: // add
                        Client.instance.map[key] = {
                            x: p.x,
                            y: p.y,
                            tiles: p.tiles,
                            isChanged: false
                        };
                        break
                    case 2: // change
                        Client.instance.map[key].tiles = p.tiles;
                        Client.instance.map[key].isChanged = true
                        Game.instance?.addGrid(p.x, p.y)
                        break
                }
                break;
            }
            case ServerPacket.MAP_CONFIRMED : {
                for (let mapKey in Client.instance.map) {
                    let s = mapKey.split("_");
                    let x: number = +s[0];
                    let y: number = +s[1];
                    Game.instance?.addGrid(x, y)
                }
                break
            }
            case ServerPacket.OBJECT_ADD: {
                const old = Client.instance.objects[data.id]
                Client.instance.objects[data.id] = data
                if (old !== undefined) {
                    Client.instance.objects[data.id].moveController = old.moveController
                    Client.instance.objects[data.id].view = old.view
                }
                Game.instance?.onObjectAdd(Client.instance.objects[data.id])
                Game.instance?.updateMapScalePos()
                break;
            }
            case ServerPacket.OBJECT_DELETE: {
                let obj = Client.instance.objects[(<ObjectDel>data).id];
                obj.moveController?.stop()
                Game.instance?.onObjectDelete(obj)
                delete Client.instance.objects[(<ObjectDel>data).id];
                break;
            }
            case ServerPacket.OBJECT_MOVE : {
                let obj = Client.instance.objects[data.id];
                if (obj !== undefined) {
                    if (obj.moveController === undefined) {
                        obj.moveController = new MoveController(obj, (<ObjectMoved>data))
                    } else {
                        obj.moveController.applyData((<ObjectMoved>data))
                    }
                }
                break;
            }
            case ServerPacket.OBJECT_STOP : {
                let obj = Client.instance.objects[data.id];
                if (obj !== undefined) {
                    if (obj.moveController !== undefined) {
                        obj.moveController.serverStop(<ObjectStopped>data)
                    } else {
                        console.warn("stopped: set pos")
                        obj.x = data.x
                        obj.y = data.y
                        Game.instance?.onObjectMoved(obj)
                    }
                }

                Game.instance?.updateMapScalePos()
                break;
            }
            case ServerPacket.STATUS_UPDATE : {
                let statusUpdate = <StatusUpdate>data
                if (statusUpdate.id == Client.instance.selectedCharacterId) {
                    for (let i = 0; i < statusUpdate.list.length; i++) {
                        Client.instance.myStatus[statusUpdate.list[i].i] = statusUpdate.list[i].v
                    }
                    Game.instance?.onMyStatusUpdate()
                }
                break
            }
            case ServerPacket.CONTEXT_MENU : {
                let cm = <ContextMenuData>data
                let obj = Client.instance.objects[cm.id];
                if (obj !== undefined) {
                    cm.obj = obj
                    Game.instance?.makeContextMenu(cm)
                } else {
                    Game.instance?.makeContextMenu(undefined)
                }
                break
            }
            case ServerPacket.ACTION_PROGRESS : {
                let ap = <ActionProgressData>data
                Game.instance?.setActionProgress(ap)
                break
            }
            case ServerPacket.INVENTORY_UPDATE : {
                let inv = <InventoryUpdate>data
                store.commit(MutationTypes.INVENTORY_UPDATE, inv)
                break
            }
            case ServerPacket.INVENTORY_CLOSE : {
                store.commit(MutationTypes.INVENTORY_CLOSE, data.id)
                break
            }
            case ServerPacket.PLAYER_HAND : { // player hand
                let hd = <HandData>data
                if (hd.icon !== undefined) {
                    store.commit(MutationTypes.SET_HAND, hd)
                } else {
                    store.commit(MutationTypes.SET_HAND, undefined)
                }
                break
            }
            case ServerPacket.CREATURE_SAY : {
                let obj = Client.instance.objects[data.id]
                // канал в который пришло сообщение
                let c = data.c == 0xff ? "System" : ((obj !== undefined && obj.a !== undefined) ? obj.a.n : "unknown")
                let msg = c + ": " + data.t
                Client.instance.chatHistory.unshift(msg)
                Client.instance.chatHistory.splice(7)
                Client.instance.onChatMessage?.()
                break;
            }

            case ServerPacket.FILE_CHANGED : {
                let f: string = data.f
                for (let key in Client.instance.objects) {
                    Client.instance.objects[key].view?.onAssetsChanged(f)
                }
                Game.instance?.onFileChange(f)
            }
        }
    }
}
