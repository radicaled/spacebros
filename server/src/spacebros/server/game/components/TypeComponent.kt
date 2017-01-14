package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentMetadata

@ComponentMetadata("type", clientSync = true)
class TypeComponent : Component() {
    @ClientValue("name")
    var name = "[UNDEFINED]"
}
