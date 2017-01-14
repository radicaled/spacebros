package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentMetadata

@ComponentMetadata("position", clientSync = true)
class PositionComponent : Component() {
    @ClientValue("x") var x = 0
    @ClientValue("y") var y = 0
    @ClientValue("z") var z = 0
}
