package spacebros.networking

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object Messages {
    val mapper = jacksonObjectMapper()
            // TODO: wtf did I enable this for? was it for the libgdx client?
//            .enableDefaultTyping()
            .registerKotlinModule().apply {
                enable(SerializationFeature.INDENT_OUTPUT)
    }

//    fun encode(message: Any): String = mapper.writeValueAsString(message)
//    fun decode(message: String): Any = mapper.readValue(message, typeMap)

    fun encode(message: RootMessage): String = mapper.writeValueAsString(message)
    fun decode(message: String): Any = mapper.readValue(message, RootMessage::class.java)

    data class Position(val x: Int, val y: Int, val z: Int) {
        companion object {
            val ZERO = Position(0, 0, 0)
        }
    }

    data class Graphic(val tileId: Int)

    enum class Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    enum class TextType {
        MESSAGE,
        SPEAK
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    interface RootMessage

    data class Login(val playerName: String, val data: String) : RootMessage
    data class LoginFail(val reason: String) : RootMessage
    data class LoginSuccess(val welcomeMessage: String) : RootMessage

    class SynchronizeRequest : RootMessage

    data class CreateEntity(val entityId: Int, val state: Map<String, Any>) : RootMessage
    data class UpdateEntity(val entityId: Int, val state: Map<String, Any>) : RootMessage
    data class DeleteEntity(val entityId: Int) : RootMessage

    data class SetCamera(val position: Position) : RootMessage

    data class MoveDirection(val direction: Direction) : RootMessage
    data class MoveToPosition(val entityId: Int, val position: Position) : RootMessage

    data class TextMessage(val message: String, val textType: TextType) : RootMessage {
        var entityId: Int? = null
    }

    data class Interaction(val entityId: Int, val action: String) : RootMessage

    data class Animate(val entityId: Int, val animation: String) : RootMessage
}

