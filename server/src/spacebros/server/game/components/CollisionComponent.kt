package spacebros.server.game.components

import com.artemis.Component

/**
 * Describes an entity that can be collided against
 */
class CollisionComponent(defaultCollisionState: CollisionState = CollisionState.ACTIVE) : Component() {
    enum class CollisionState {
        ACTIVE,
        INACTIVE
    }

    var collisionState = defaultCollisionState
}
