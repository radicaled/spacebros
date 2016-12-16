package spacebros.server.game.behaviors

import com.artemis.World

class BehaviorRegistry(val world: World) {
    val behaviors = hashMapOf<String, Behavior>()

    fun has(type: String): Boolean {
        return behaviors.containsKey(type)
    }

    fun register(type: String, behavior: Behavior) {
        if (has(type)) throw IllegalArgumentException("A behavior instance for $type already exists!")
        world.inject(behavior)
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
fun makeBehaviorRegistry(world: World): BehaviorRegistry {
    val behaviorRegistry = BehaviorRegistry(world)

    behaviorRegistry.register("door", DoorBehavior(world))
    behaviorRegistry.register("lootable", LootableBehavior(world))

    return behaviorRegistry
}
