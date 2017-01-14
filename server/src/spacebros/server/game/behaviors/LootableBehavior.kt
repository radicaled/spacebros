package spacebros.server.game.behaviors

import com.artemis.Aspect
import com.artemis.World
import com.artemis.annotations.Wire
import spacebros.networking.Messages
import spacebros.server.game.ConnectionHub
import spacebros.server.game.Intent
import spacebros.server.game.active
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
        val inventoryComponent = invokingEntity.getComponent(InventoryComponent::class.java)

        inventoryComponent.contents.add(lootableEntity.id)
        lootableEntity.edit().remove(VisibilityComponent::class.java)

        val playerStateMessage = Messages.UpdateEntity(
                intent.invokingEntityId,
                ClientSerializer().serialize(inventoryComponent)
        )

        val message = Messages.DeleteEntity(intent.targetEntityId)
        val entityAspects = Aspect.all(PlayerComponent::class.java)
        val playerEntities = world.aspectSubscriptionManager.get(entityAspects).entities

        // TODO: more performant way of updating players?
        playerEntities.active().forEach { entityId ->
            if (entityId != intent.invokingEntityId)
                hub.send(entityId, message)
        }
        hub.send(intent.invokingEntityId, playerStateMessage)
    }
}
