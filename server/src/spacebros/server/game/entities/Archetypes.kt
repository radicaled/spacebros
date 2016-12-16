package spacebros.server.game.entities

import com.artemis.ArchetypeBuilder
import spacebros.server.game.components.*

object Archetypes {
    val visual: ArchetypeBuilder = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(NameComponent::class.java)
            .add(VisibilityComponent::class.java)

    val player: ArchetypeBuilder = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)
            .add(InventoryComponent::class.java)
            .add(VisibilityComponent::class.java)

    val wall: ArchetypeBuilder = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)
            .add(VisibilityComponent::class.java)

    val door: ArchetypeBuilder = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)
            .add(DoorComponent::class.java)
            .add(VisibilityComponent::class.java)

    val butcherKnife: ArchetypeBuilder = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)
            .add(DoorComponent::class.java)
            .add(VisibilityComponent::class.java)
            .add(MovableComponent::class.java)
}
