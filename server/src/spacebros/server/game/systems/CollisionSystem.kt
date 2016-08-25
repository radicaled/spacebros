package spacebros.server.game.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import spacebros.networking.Messages
import spacebros.server.game.components.CollisionComponent
import spacebros.server.game.components.MovementComponent
import spacebros.server.game.components.PositionComponent
import spacebros.server.game.components.TypeComponent

/**
 * Prevents entities from passing other other solid entities.
 */
class CollisionSystem() : IteratingSystem(aspects) {
    companion object {
        val aspects: Aspect.Builder = Aspect.all(MovementComponent::class.java,
                PositionComponent::class.java, CollisionComponent::class.java)
        val collidableEntityAspect: Aspect.Builder by lazy {
            Aspect.all(PositionComponent::class.java, CollisionComponent::class.java)
        }
    }

    lateinit var posMapper: ComponentMapper<PositionComponent>
    lateinit var moveMapper: ComponentMapper<MovementComponent>
    lateinit var tMapper: ComponentMapper<TypeComponent>

    override fun process(entityId: Int) {
        val pc = posMapper.get(entityId)
        val mc = moveMapper.get(entityId)

        var desiredX = pc.x
        var desiredY = pc.y
        //var desiredZ = pc.z

        // TODO: don't compute movement coordinates by hand -- precompute in an earlier system?
        // NOTE: z layers not currently supported
        when(mc.direction) {
            Messages.Direction.NORTH -> desiredY -= 1
            Messages.Direction.SOUTH -> desiredY += 1
            Messages.Direction.EAST -> desiredX += 1
            Messages.Direction.WEST -> desiredX -= 1
        }

        // do any other entities unpassable entities exist at the desired x, y, z coordinates?
        val collidableEntities = world.aspectSubscriptionManager.get(collidableEntityAspect).entities
        val hasCollided = collidableEntities.data.any {
            if(it == entityId) return@any false
            val otherEntityPosition = posMapper.get(it)
            otherEntityPosition.x == desiredX && otherEntityPosition.y == desiredY
        }

        if (hasCollided) {
            moveMapper.remove(entityId)
        }
    }
}
