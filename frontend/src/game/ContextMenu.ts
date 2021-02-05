import {ContextMenuData} from "@/net/Packets";
import * as PIXI from 'pixi.js';
import Net from "@/net/Net";

export default class ContextMenu {
    /**
     * расстояние между кнопками по вертикали
     */
    private static readonly ITEM_HEIGHT = 40

    /**
     * отступ текста по краям по горизонтали
     */
    private static readonly TEXT_PADDING = 6

    private readonly cm: ContextMenuData

    public container: PIXI.Container

    constructor(cm: ContextMenuData) {
        this.cm = cm

        this.container = new PIXI.Container()

        const offy = (cm.l.length * ContextMenu.ITEM_HEIGHT) / 2
        for (let i = 0; i < cm.l.length; i++) {
            this.makeButton(cm.l[i], 14, ContextMenu.ITEM_HEIGHT * i - offy)
        }
    }

    public destroy() {
        this.container.destroy({
            children: true
        })
    }

    private makeButton(t: string, x: number, y: number) {
        let spr1 = PIXI.Sprite.from("context_menu_1")
        let spr2 = PIXI.Sprite.from("context_menu_2")
        let spr3 = PIXI.Sprite.from("context_menu_3")

        let text = new PIXI.Text(t, {
            fill: "#ddd0b0",
            fontFamily: "Verdana",
            fontSize: 15
        })
        text.x = x + spr1.width + ContextMenu.TEXT_PADDING
        text.y = y + 5


        spr1.x = x
        spr1.y = y

        spr2.x = spr1.x + spr1.width
        spr2.y = spr1.y
        spr2.width = text.width + ContextMenu.TEXT_PADDING * 2

        spr3.x = spr2.x + spr2.width
        spr3.y = spr1.y

        this.container.addChild(spr1)
        this.container.addChild(spr2)
        this.container.addChild(spr3)
        this.container.addChild(text)

        spr1.interactive = true
        spr2.interactive = true
        spr3.interactive = true

        spr1.on("click", () => {
            this.click(t)
        })
        spr1.on("tap", () => {
            this.click(t)
        })
        spr2.on("click", () => {
            this.click(t)
        })
        spr2.on("tap", () => {
            this.click(t)
        })
        spr3.on("click", () => {
            this.click(t)
        })
        spr3.on("tap", () => {
            this.click(t)
        })
    }

    private click(t: string) {
        console.log("cl")
        Net.remoteCall("cmselect", {
            item: t
        })
        this.destroy()
    }
}