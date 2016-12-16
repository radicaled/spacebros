package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName

// TODO: optimize this --> shouldn't be sent to client all the time.
@ComponentName("visibility")
class VisibilityComponent(defaultVisibility: Visibility = Visibility.Visible) : Component() {
    enum class Visibility {
        Visible,
        Invisible
    }
    @ClientValue("visibility")
    var visibility = defaultVisibility
}
