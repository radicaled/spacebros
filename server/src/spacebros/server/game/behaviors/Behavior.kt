package spacebros.server.game.behaviors

import com.artemis.World
import spacebros.server.game.ConnectionHub
import spacebros.server.game.Intent

abstract class Behavior() {
    // TODO: ... ugh, do something about the connectionhub.
    abstract fun execute(world: World, intent: Intent, hub: ConnectionHub)
}
