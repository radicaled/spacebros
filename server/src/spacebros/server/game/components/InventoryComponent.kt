package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName
import java.util.*

@ComponentName("inventory")
class InventoryComponent : Component() {
    // TODO: only sync this to clients that need to know about inventory contents
    @ClientValue("contents")
    val contents: HashSet<Int> = hashSetOf()
}
