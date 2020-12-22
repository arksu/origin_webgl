package com.origin.model

/**
 * позиция объекта в игровом мире
 */
class Position(var x: Int, var y: Int, var level: Int, var region: Int, var heading: Int, val parent: GameObject) {

}