package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName

@ComponentName("type")
class TypeComponent : Component() {
    @ClientValue("name")
    var name = "[UNDEFINED]"
}
