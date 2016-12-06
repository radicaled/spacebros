package spacebros.server.game.entities

import com.artemis.Archetype
import com.artemis.ArchetypeBuilder
import com.artemis.World

class ArchetypeRegistry(val world: World) {
    val archetypes = hashMapOf<String, Archetype>()

    fun has(type: String): Boolean {
        return archetypes.containsKey(type)
    }

    fun register(type: String, builder: ArchetypeBuilder) {
        if (has(type)) throw IllegalArgumentException("An archetype for $type already exists!")
        archetypes[type] = builder.build(world)
    }

    fun get(type: String): Archetype {
        val archetype = archetypes[type]
        if (archetype != null) return archetype
        throw IllegalArgumentException("No archetype for $type found.")
    }
}

// TODO: need to move assembly of archetypes into an s-expression based file
// or maybe just some groovy scripts.
fun makeRegistry(world: World): ArchetypeRegistry {
    val archetypeRegistry = ArchetypeRegistry(world)

    archetypeRegistry.register("player", Archetypes.player)
    archetypeRegistry.register("visual", Archetypes.visual)
    archetypeRegistry.register("walls", Archetypes.wall)
    archetypeRegistry.register("door", Archetypes.door)
    archetypeRegistry.register("Doorcomglass", Archetypes.door)
    archetypeRegistry.register("Doorsecglass", Archetypes.door)

    return archetypeRegistry
}
