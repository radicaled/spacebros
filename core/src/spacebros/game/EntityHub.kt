package spacebros.game

import spacebros.game.entities.Entity

class EntityHub {
    val entities = hashMapOf<Int, Entity>()

    fun register(remoteEntityId: Int, entity: Entity) {
        entities[remoteEntityId] = entity
    }

    fun deregister(remoteEntityId: Int) {
        entities.remove(remoteEntityId)
    }

    fun find(remoteEntityId: Int): Entity {
        return entities.getOrElse(remoteEntityId) {
            throw IllegalArgumentException("entity not found: $remoteEntityId")
        }
    }
}
