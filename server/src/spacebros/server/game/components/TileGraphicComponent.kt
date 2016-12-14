package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName

@ComponentName("tileGraphic")
class TileGraphicComponent : Component() {
    var graphicFile: String = ""
    @ClientValue("tileId")
    var tileId: Int = -1
}
