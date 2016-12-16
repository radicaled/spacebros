package spacebros.server.game.behaviors

import com.artemis.Aspect
import com.artemis.World
import com.artemis.annotations.Wire
import spacebros.networking.Messages
import spacebros.server.game.ConnectionHub
import spacebros.server.game.Intent
import spacebros.server.game.components.ClientSerializer
import spacebros.server.game.components.InventoryComponent
import spacebros.server.game.components.PlayerComponent
import spacebros.server.game.components.VisibilityComponent

class LootableBehavior(override val world: World) : Behavior() {
    @Wire(name = "ConnectionHub")
    lateinit var hub: ConnectionHub

    override fun execute(intent: Intent) {
        when(intent.actionName) {
            "pick_up" -> handlePickUp(intent)
        }
    }

    private fun handlePickUp(intent: Intent) {
        val invokingEntity = world.getEntity(intent.invokingEntityId)
        val lootableEntity = world.getEntity(intent.targetEntityId)
        // TODO: Ensure invoker has an InventoryComponent
        // TODO: if this item was in another InventoryComponent, remove it.
        // TODO: ensure this item has a visibilitycomponent
        // TODO: should we remove this from the list of things all clients know about?
        // TODO: hiding entities instead of revoking them lets other clients cheat?
        val inventoryComponent = invokingEntity.getComponent(InventoryComponent::class.java)
//        val visibilityComponent = lootableEntity.getComponent(VisibilityComponent::class.java)

        inventoryComponent.contents.add(lootableEntity.id)
//        visibilityComponent.visibility = VisibilityComponent.Visibility.Invisible
        lootableEntity.edit().remove(VisibilityComponent::class.java)

        val lootableStateMessage = Messages.UpdateEntity(
                intent.invokingEntityId,
                ClientSerializer().serialize(inventoryComponent)
        )
        // TODO: ew, hack.
        // TODO: remove entity information from everyone but the person who owns it.
        val message = Messages.DeleteEntity(intent.targetEntityId)
        val entityAspects = Aspect.all(PlayerComponent::class.java)
        val playerEntities = world.aspectSubscriptionManager.get(entityAspects).entities
        playerEntities.data.forEach { entityId ->
            if (entityId != intent.invokingEntityId)
                hub.send(entityId, message)
        }
        hub.send(intent.invokingEntityId, lootableStateMessage)
    }
}
