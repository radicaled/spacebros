package spacebros.server.game.components

import com.artemis.Component

class DoorComponent(defaultDoorState: DoorState = DoorState.OPEN) : Component() {
    enum class DoorState {
        OPEN,
        CLOSED
    }

    var doorState = defaultDoorState
}
