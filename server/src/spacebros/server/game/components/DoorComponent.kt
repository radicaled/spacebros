package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientComponent
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentMetadata

@ComponentMetadata("door", clientSync = true)
class DoorComponent(defaultDoorState: DoorState = DoorState.CLOSED) : Component() {
    enum class DoorState {
        OPEN,
        CLOSED
    }

    @ClientValue("doorState")
    var doorState = defaultDoorState
}
