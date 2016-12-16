package spacebros.server.game.components

import com.artemis.Component

class VisibilityComponent(defaultVisibility: Visibility = Visibility.Visible) : Component() {
    enum class Visibility {
        Visible,
        Invisible
    }

    var visibility = defaultVisibility
}
