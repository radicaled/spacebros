package spacebros.game.components

import com.artemis.Component
import kotlin.properties.Delegates

class RemoteEntityComponent : Component() {
    var entityId by Delegates.notNull<Int>()
}
