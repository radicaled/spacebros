package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName

@ComponentName("door")
class DoorComponent(defaultDoorState: DoorState = DoorState.OPEN) : Component() {
    enum class DoorState {
        OPEN,
        CLOSED
    }

    @ClientValue("doorState")
    var doorState = defaultDoorState
}
