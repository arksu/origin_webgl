package com.origin.entity

/**
 * предмет в инвентаре
 */
//@Entity
//@Table(name = "inventory")
class InventoryItem {
    //    @Id
//    @Column(name = "id", columnDefinition = "INT(11) NOT NULL AUTO_INCREMENT")
    val id = 0

    /**
     * ид инвентаря (родителя, вещи в которой находится этот предмет
     */
//    @Column(name = "inventoryId", columnDefinition = "INT(11) NOT NULL")
    var inventoryId = 0

    /**
     * тип предмета
     */
//    @Column(name = "type", columnDefinition = "INT(11) NOT NULL")
    var type = 0

    /**
     * положение внутри инвентаря
     */
//    @Column(name = "x", columnDefinition = "INT(11) NOT NULL")
    var x = 0

    //    @Column(name = "y", columnDefinition = "INT(11) NOT NULL")
    var y = 0

    /**
     * качество вещи
     */
//    @Column(name = "quality", columnDefinition = "INT(11) NOT NULL")
    var quality = 0

    /**
     * количество в стаке
     */
//    @Column(name = "count", columnDefinition = "INT(11) NOT NULL")
    var count = 0

    /**
     * тик (если вещь может имзенятся с течением времени
     */
//    @Column(name = "tick", columnDefinition = "INT(11) NOT NULL")
    var tick = 0
}