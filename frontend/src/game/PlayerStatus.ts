import * as PIXI from 'pixi.js';
import Game from "@/game/Game";
import Client from "@/net/Client";

/**
 * отображение текущего статуса моего персонажа
 */
export default class PlayerStatus {
    private static readonly BAR_WIDTH = 78

    iconFrame: PIXI.Sprite
    statusHud: PIXI.Sprite
    icon: PIXI.Sprite

    hp: PIXI.Graphics
    stamina: PIXI.Graphics

    constructor(game: Game) {
        this.iconFrame = PIXI.Sprite.from("player_icon_frame")
        this.iconFrame.x = 4
        this.iconFrame.y = 4

        this.icon = PIXI.Sprite.from("micon_bear")
        this.icon.x = this.iconFrame.x + 6
        this.icon.y = this.iconFrame.y + 6

        game.app.stage.addChild(this.icon)
        game.app.stage.addChild(this.iconFrame)

        this.statusHud = PIXI.Sprite.from("status_hud")
        this.statusHud.x = this.iconFrame.x + this.iconFrame.width + 3
        this.statusHud.y = this.iconFrame.y + 6
        game.app.stage.addChild(this.statusHud)


        this.hp = new PIXI.Graphics()
        this.hp.x = this.statusHud.x + 8
        this.hp.y = this.statusHud.y + 4
        game.app.stage.addChild(this.hp)

        this.stamina = new PIXI.Graphics()
        this.stamina.x = this.statusHud.x + 102
        this.stamina.y = this.statusHud.y + 4
        game.app.stage.addChild(this.stamina)

        this.update()
    }

    public update() {
        const shp = Client.instance.myStatus[0]
        const hhp = Client.instance.myStatus[1]
        const maxhp = Client.instance.myStatus[2]
        const stamina = Client.instance.myStatus[3]
        const maxStamina = Client.instance.myStatus[4]

        this.hp.clear()

        this.hp.beginFill(0xdbcb39)
        this.hp.drawRect(0, 0, hhp / maxhp * PlayerStatus.BAR_WIDTH, 4)

        this.hp.beginFill(0xe74c3c)
        this.hp.drawRect(0, 0, shp / maxhp * PlayerStatus.BAR_WIDTH, 4)
        this.hp.endFill()

        this.stamina.clear()
        this.stamina.beginFill(0x1684fa)
        this.stamina.drawRect(0, 0, stamina / maxStamina * PlayerStatus.BAR_WIDTH, 4)
        this.stamina.endFill()

    }
}