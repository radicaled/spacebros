package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentMetadata

@ComponentMetadata("name", clientSync = true)
class NameComponent(@ClientValue("name") var name: String = "Unknown Object") : Component()
