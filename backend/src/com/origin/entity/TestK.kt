package com.origin.entity

import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "testk", indexes = [Index(name = "id_uniq", columnList = "instance, x, y, level", unique = true)])
class TestK {
    /**
     * на каком континенте находится грид, либо ид дома (инстанса, локации)
     */
    @Column(name = "instance", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    private val _instanceId = 0

    /**
     * координаты грида в мире (какой по счету грид, НЕ в игровых единицах)
     * разбиение таблицы (partitions) делаем на основе RANGE(x) и субпартициях на основе RANGE(y)
     */
    @Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    private val _x = 0

    @Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    private val _y = 0

    @Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    private val _level = 0

    /**
     * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
     */
    @Column(name = "tiles", columnDefinition = "BLOB NOT NULL", nullable = false)
    private val _tilesBlob: Blob? = null
}