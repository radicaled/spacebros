package spacebros.game.components

import com.artemis.Component
import com.badlogic.gdx.math.Vector2

class PositionComponent() : Component() {
    constructor(vector2: Vector2) : this() {
        this.vector = vector2
    }
    var vector: Vector2? = null
}
