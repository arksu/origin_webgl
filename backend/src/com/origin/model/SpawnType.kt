package com.origin.model

enum class SpawnType {
    // точно в указанную точку (координаты объекта)
    EXACTLY_POINT,
    // рядом с координатами
    NEAR,
    // в рандомной точке того же региона (level=0)
    RANDOM_SAME_REGION
}