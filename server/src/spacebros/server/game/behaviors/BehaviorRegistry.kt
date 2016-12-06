package spacebros.server.game.behaviors

import com.artemis.World

class BehaviorRegistry() {
    val behaviors = hashMapOf<String, Behavior>()

    fun has(type: String): Boolean {
        return behaviors.containsKey(type)
    }

    fun register(type: String, behavior: Behavior) {
        if (has(type)) throw IllegalArgumentException("A behavior instance for $type already exists!")
        behaviors[type] = behavior
    }

    fun get(type: String): Behavior {
        val behavior = behaviors[type]
        if (behavior != null) return behavior
        throw IllegalArgumentException("No behavior for $type found.")
    }
}

// TODO: need to move assembly of behaviors into an s-expression based file
// or maybe just some groovy scripts.
fun makeBehaviorRegistry(): BehaviorRegistry {
    val behaviorRegistry = BehaviorRegistry()

    behaviorRegistry.register("door", DoorBehavior())

    return behaviorRegistry
}
