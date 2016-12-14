package spacebros.server.game.behaviors

import com.artemis.World
import com.artemis.annotations.Wire
import spacebros.server.game.Intent

abstract class Behavior() {
    abstract val world: World

    abstract fun execute(intent: Intent)
}
