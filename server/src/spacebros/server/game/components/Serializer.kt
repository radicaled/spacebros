package spacebros.server.game.components

import com.artemis.Component
import spacebros.server.game.components.annotations.ClientValue
import spacebros.server.game.components.annotations.ComponentName
import kotlin.reflect.jvm.properties
import kotlin.reflect.memberProperties

class ClientSerializer {
    fun serialize(vararg components: Component): Map<String, Map<String, Any>> {
        return serialize(components.asIterable())
    }

    fun serialize(components: Iterable<Component>): Map<String, Map<String, Any>> {
        return components
                .filter { canSerialize(it) }
                .map { serialize(it) }
                .filter { it.second.isNotEmpty() }
                .toMap()
    }

    fun canSerialize(component: Component): Boolean {
        val klass = component.javaClass
        return klass.isAnnotationPresent(ComponentName::class.java)
    }

    private fun serialize(component: Component) : Pair<String, Map<String, Any>> {
        val klass = component.javaClass
        val componentName = klass.getAnnotation(ComponentName::class.java).name
        println("Fields in ${klass.name}: ${klass.fields.count()}")

        val componentFieldData = klass.fields
                .filter { it.isAnnotationPresent(ClientValue::class.java) }
                .map { it.getAnnotation(ClientValue::class.java).name to it.get(component) }
                .toMap()
        val componentMethodData = klass.methods
                .filter { it.isAnnotationPresent(ClientValue::class.java) }
                .filter { it.parameterCount == 0 }
                .map { it.getAnnotation(ClientValue::class.java).name to it.invoke(null) }
                .toMap()
        val componentPropertyData = klass.kotlin.memberProperties
                .filter { it.annotations.any { it is ClientValue } }
                .map {
                    val cv = it.annotations.first { it is ClientValue } as ClientValue
                    cv.name to it.get(component)!!
                }
                .toMap()
        return componentName to (componentFieldData + componentMethodData + componentPropertyData)
    }
}
