package spacebros.game

class EntityHub {
    val entities = hashMapOf<Int, Int>()

    fun register(remoteEntityId: Int, localEntityId: Int) {
        entities[remoteEntityId] = localEntityId
    }

    fun deregister(remoteEntityId: Int) {
        entities.remove(remoteEntityId)
    }

    fun find(remoteEntityId: Int): Int {
        return entities.getOrElse(remoteEntityId) {
            throw IllegalArgumentException("entity not found: $remoteEntityId")
        }
    }
}
