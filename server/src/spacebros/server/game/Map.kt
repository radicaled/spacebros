package spacebros.server.game

import java.util.*

class Map {
    val entities = arrayListOf<MapEntity>()
}

class MapEntity {
    var type: String? = null
    var graphic: Int? = null
    var position: MapPosition? = null
    val behaviors: ArrayList<String> = arrayListOf()
}

class MapPosition {
    var x: Int? = null
    var y: Int? = null
    var z: Int = 0
}
