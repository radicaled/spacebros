package spacebros.server.game.components

import com.artemis.Component
import spacebros.networking.Messages
import kotlin.properties.Delegates

class MovementComponent() : Component() {
    constructor(direction: Messages.Direction) : this() {
        this.direction = direction
    }
    var direction by Delegates.notNull<Messages.Direction>()
}
