package spacebros.server.game.behaviors

import com.artemis.World
import com.artemis.annotations.Wire
import spacebros.networking.Messages
import spacebros.server.game.ConnectionHub
import spacebros.server.game.Intent
import spacebros.server.game.components.ClientSerializer
import spacebros.server.game.components.CollisionComponent
import spacebros.server.game.components.DoorComponent

class DoorBehavior(override val world: World) : Behavior() {
    @Wire(name = "ConnectionHub")
    lateinit var hub: ConnectionHub

    override fun execute(intent: Intent) {
        when(intent.actionName) {
            "use" -> handleOpenAction(intent)
            else -> print("Received this: ${intent.actionName}")
        }
    }

    fun handleOpenAction(intent: Intent) {
        val doorEntity = world.getEntity(intent.targetEntityId)
        val doorComponent = doorEntity.getComponent(DoorComponent::class.java)
        if (doorComponent != null) {
            print("Dummy routine to check door access")
            // TODO: test code; just remove the component and open the door?
            // TODO: animation subroutine
            // TODO: remove hard-coding

            // TODO: cleanup?
            val animationName: String
            if (doorComponent.doorState == DoorComponent.DoorState.OPEN) {
                doorComponent.doorState = DoorComponent.DoorState.CLOSED
                doorEntity.edit().add(CollisionComponent())
                animationName = "close"
            } else {
                doorComponent.doorState = DoorComponent.DoorState.OPEN
                doorEntity.edit().remove(CollisionComponent::class.java)
                animationName = "open"
            }

            val animateMessage = Messages.Animate(
                    intent.targetEntityId,
                    animationName
            )
            val stateMessage = Messages.UpdateEntity(
                    intent.targetEntityId,
                    ClientSerializer().serialize(doorComponent)
            )
            hub.broadcast(animateMessage)
            hub.broadcast(stateMessage)

            // TODO: implement the below
            // Check MY access level, if any
            // Check THEIR access level, if any
        } else {
            // Just open
            print("Dummy routine to open door")
        }
    }
}
