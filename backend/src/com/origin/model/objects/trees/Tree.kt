package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import com.origin.entity.InventoryItemEntity
import com.origin.model.*
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.model.inventory.ItemType
import com.origin.net.model.ActionProgress
import com.origin.utils.Rect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Деревья
 */
@ObsoleteCoroutinesApi
abstract class Tree(entity: EntityObject) : StaticObject(entity) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Tree::class.java)
    }

    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int = entity.data?.toInt() ?: 6

    override val inventory: Inventory? = null

    override fun contextMenu(p: Player): ContextMenu {
        return ContextMenu(this, "Chop", "Take branch", "Take bark")
    }

    override fun getBoundRect(): Rect {
        return Rect(10)
    }

    override suspend fun processContextItem(player: Player, item: String) {
        logger.debug("processContextItem $player $item")
        when (item) {
            "Chop" -> {
                player.startAction(this, 3, getMaxHP() - this.entity.hp, getMaxHP(), {
                    // возьмем у игрока часть стамины и голода
                    it.status.checkAndReduceStamina(4.0)
                }) {
                    var done = false
                    if (it.target is Tree) {
                        // не удалось снять очередные хп с дерева
                        if (!it.target.takeHP(6)) {
                            // значит хп кончилось. и дерево срубили
                            logger.warn("TREE CHOPPED!")
                            // TODO make tree -> stump
                            transaction {
                                it.target.entity.data = "10"
                                it.target.entity.hp = 120
                            }
                            it.target.stage = 10
                            // уведомим окружающие объекты о том что это дерево изменилось
                            it.target.grid.broadcast(BroadcastEvent.Changed(it.target))
                            done = true
                        } else {
                            it.sendPkt(ActionProgress(it.maxProgress - it.target.entity.hp, it.maxProgress))
                        }
                    }
                    done
                }
            }
            "Take branch" -> {
                player.startAction(this, -2, 0, 21, {
                    // возьмем у игрока часть стамины
                    it.status.checkAndReduceStamina(1.0)
                }) {
                    // TODO generate branch to players inventory
                    logger.warn("GEN BRANCH")

                    val newItem = transaction {
                        val e = InventoryItemEntity.makeNew(ItemType.BRANCH)
                        InventoryItem(e, null)
                    }
                    val result = CompletableDeferred<Boolean>()
                    player.send(GameObjectMsg.PutItem(newItem, -1, -1, result))
                    result.await()
                    true
                }
            }
            "Take bark" -> {
                player.startAction(this, -2, 0, 3, {
                    // возьмем у игрока часть стамины
                    it.status.checkAndReduceStamina(1.0)
                }) {
                    // TODO generate bark to players inventory

                    val newItem = transaction {
                        val e = InventoryItemEntity.makeNew(ItemType.BARK)
                        InventoryItem(e, null)
                    }

                    val result = CompletableDeferred<Boolean>()
                    player.send(GameObjectMsg.PutItem(newItem, -1, -1, result))
                    result.await()
                    true
                }
            }
        }
    }
}