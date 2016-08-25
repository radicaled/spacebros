package spacebros.server.game.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import spacebros.networking.Messages
import spacebros.server.game.ConnectionHub
import spacebros.server.game.components.MovementComponent
import spacebros.server.game.components.PositionComponent

class MoveSystem(val connectionHub: ConnectionHub): IteratingSystem(aspects) {
    companion object {
        val aspects: Aspect.Builder = Aspect.all(MovementComponent::class.java,
                PositionComponent::class.java)
    }

    lateinit var posMapper: ComponentMapper<PositionComponent>
    lateinit var moveMapper: ComponentMapper<MovementComponent>

    override fun process(entityId: Int) {
        val pc = posMapper.get(entityId)
        val mc = moveMapper.get(entityId)

        print("Moving FROM ${pc.x}/${pc.y}")

        when(mc.direction) {
            Messages.Direction.NORTH -> pc.y -= 1
            Messages.Direction.SOUTH -> pc.y += 1
            Messages.Direction.EAST -> pc.x += 1
            Messages.Direction.WEST -> pc.x -= 1
        }
        print("Moving TO ${pc.x}/${pc.y}")

        moveMapper.remove(entityId)

        val setCamera = Messages.SetCamera(Messages.Position(pc.x, pc.y, pc.z))
        val moveTo = Messages.MoveToPosition(entityId, Messages.Position(pc.x, pc.y, pc.z))
        connectionHub.send(entityId, setCamera)
        connectionHub.broadcast(moveTo)
    }
}
