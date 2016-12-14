package spacebros.server.game.behaviors

import com.artemis.World
import com.artemis.annotations.Wire
import spacebros.networking.Messages
import spacebros.server.game.ConnectionHub
import spacebros.server.game.Intent
import spacebros.server.game.components.CollisionComponent
import spacebros.server.game.components.DoorComponent
import spacebros.server.game.components.TileGraphicComponent

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



            // TODO: START TERRIBLE IDEA
            val tgc = doorEntity.getComponent(TileGraphicComponent::class.java)
            tgc.tileId = 7 // hard-coded for security door sprite
            // TODO: there's some caching involved in removing a component
            // TODO: so sometimes the collision component comes back or isn't removed -- can't remember what
            // TODO: the artemis docs say
            doorEntity.edit().remove(CollisionComponent::class.java)
            // I can either:
            // - pass down the ConnectionHub through the chain
            // - make it a singleton (ew)
            // - put it on a singleton (also ew)
            // - believe in myself and make a sandwich instead
            // - never be a software architect
            // - it'd be great if i could store things directly on the world instance / databag...
            val message = Messages.UpdateGraphic(
                    intent.targetEntityId,
                    Messages.Graphic(
                        tileId = tgc.tileId,
                        file = tgc.graphicFile
                    )
            )
            hub.broadcast(message)
            // TODO: END TERRIBLE IDEA



            // TODO: implement the below
            // Check MY access level, if any
            // Check THEIR access level, if any
        } else {
            // Just open
            print("Dummy routine to open door")
        }
    }
}
