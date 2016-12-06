package spacebros.networking

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object Messages {
    val mapper = jacksonObjectMapper()
            .enableDefaultTyping()
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

    data class Graphic(val tileId: Int, val file: String)

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

    data class CreateEntity(val entityId: Int, val type: String,
                            val name: String?,
                            val position: Position,
                            val graphic: Graphic) : RootMessage
    data class DeleteEntity(val entityId: Int) : RootMessage

    data class SetCamera(val position: Position) : RootMessage

    data class MoveDirection(val direction: Direction) : RootMessage
    data class MoveToPosition(val entityId: Int, val position: Position) : RootMessage

    data class TextMessage(val message: String, val textType: TextType) : RootMessage {
        var entityId: Int? = null
    }

    data class Interaction(val entityId: Int, val action: String) : RootMessage
    // TODO: terrible idea or great idea
    data class UpdateGraphic(val entityId: Int, val graphic: Graphic) : RootMessage
}

