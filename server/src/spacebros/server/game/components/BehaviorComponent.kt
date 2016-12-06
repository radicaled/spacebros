package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.behaviors.Behavior
import java.util.*

class BehaviorComponent(behaviors: List<Behavior>? = null): Component() {
    val behaviors: ArrayList<Behavior> = arrayListOf()
    init {
        // TODO: please thine eyes good sir and rectify this jank
        if (behaviors != null)
            this.behaviors.addAll(behaviors)
    }
}
