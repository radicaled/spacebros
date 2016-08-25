package spacebros.server.game.entities

import com.artemis.ArchetypeBuilder
import spacebros.server.game.components.CollisionComponent
import spacebros.server.game.components.PositionComponent
import spacebros.server.game.components.TileGraphicComponent
import spacebros.server.game.components.TypeComponent

object Archetypes {
    val visual = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)

    val player = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)

    val wall = ArchetypeBuilder()
            .add(TypeComponent::class.java)
            .add(PositionComponent::class.java)
            .add(TileGraphicComponent::class.java)
            .add(CollisionComponent::class.java)
}
