package spacebros.server.game

class Map {
    val entities = arrayListOf<MapEntity>()
}

class MapEntity {
    var type: String? = null
    var graphic: Int? = null
    var position: MapPosition? = null
}

class MapPosition {
    var x: Int? = null
    var y: Int? = null
    var z: Int = 0
}
