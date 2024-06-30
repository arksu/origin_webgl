import type GameResponseDTO from '@/net/GameResponseDTO'

enum State {
  Disconnected,
  Connecting,
  Connected,
}

export default class GameClient {
  public static instance?: GameClient

  /**
   * состояние клиента сети
   * @private
   */
  private state: State = State.Disconnected

  /**
   * сокет
   */
  private socket?: WebSocket = undefined

  /**
   * каждый запрос имеет свой id, увеличиваем его на 1 при каждом запросе
   */
  private lastId: number = 0

  public onDisconnect?: () => void
  public onError?: (m: string) => void
  public onConnect?: () => void

  /**
   * ссылка для коннекта к вебсокет серверу
   * @private
   */
  private static readonly url: string = ('https:' === window.location.protocol ? 'wss:' : 'ws:') + '//'
    + window.location.hostname + ':'
    + window.location.port
    + '/api/game'

  public static createNew(): GameClient {
    if (GameClient.instance !== undefined) {
      GameClient.instance.disconnect()
    }
    GameClient.instance = new GameClient()
    return GameClient.instance
  }

  private constructor() {
    // сеть должна быть не активна для старта
    if (this.state !== State.Disconnected) {
      console.error('ws connect [' + this.state + '] wrong state')
      return
    }

    console.warn('ws connecting...')
    this.state = State.Connecting
    this.socket = new WebSocket(GameClient.url)

    this.socket.onopen = this.onopen.bind(this)
    this.socket.onclose = this.onclose.bind(this)
    this.socket.onmessage = this.onmessage.bind(this)
  }

  public disconnect() {
    this.state = State.Disconnected
    this.socket?.close()
    this.socket = undefined
    GameClient.instance = undefined
  }

  /**
   * обработка открытия сокета
   * @param {Event} _ev
   */
  private onopen(_ev: Event) {
    console.warn('ws connected')

    if (this.state !== State.Connecting) {
      throw Error('wrong state websocket on open')
    }

    this.state = State.Connected
    if (this.onConnect !== undefined) {
      this.onConnect()
    }
  }

  /**
   * обработка закрытия сокета
   * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
   * @param {CloseEvent} ev
   */
  private onclose(ev: CloseEvent) {
    console.warn('ws closed [' + ev.code + '] ' + ev.reason)

    this.state = State.Disconnected
    this.socket = undefined

    if (ev.code == 1011 && this.onError !== undefined) {
      this.onError(ev.reason)
    } else if (this.onDisconnect !== undefined) {
      this.onDisconnect()
    }
  }

  /**
   * получение сообщений от сервера в сокете
   * @param {MessageEvent} ev
   */
  private onmessage(ev: MessageEvent) {
    if (typeof ev.data === 'string') {
      const response: GameResponseDTO = JSON.parse(ev.data)
      console.log('%cRECV', 'color: #1BAC19', response)

      // пришло сообщение в общий канал (не ответ на запрос серверу)
      if (response.id === 0 && response.c !== undefined) {
        // this.onChannelMessage(response.c, response.d)
      } else {
        // // ищем в мапе по ид запроса
        // let request: Request = this.requests[response.id]
        // if (request !== undefined) {
        //   // удалим из мапы
        //   delete this.requests[response.id]
        //
        //   // ответ успешен?
        //   if (response.e === undefined) {
        //     request.resolve(response.d)
        //   } else {
        //     console.error(response.e)
        //     // иначе была ошибка, прокинем ее
        //     request.reject(response.e)
        //   }
        // }
      }
    } else {
      console.warn('unknown data type: ' + (typeof ev.data))
      console.warn('RECV', ev.data)
    }
  }
}