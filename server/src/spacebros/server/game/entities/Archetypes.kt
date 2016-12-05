package spacebros.server.game.entities

import com.artemis.ArchetypeBuilder
import spacebros.server.game.components.*

object Archetypes {
    val visual = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(NameComponent::class.java)

    val player = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)

    val wall = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
            .add(NameComponent::class.java)
}
