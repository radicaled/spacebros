package spacebros.server.game.components.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ComponentMetadata(val name: String, val clientSync: Boolean = false)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ClientComponent()

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class ClientValue(val name: String)
