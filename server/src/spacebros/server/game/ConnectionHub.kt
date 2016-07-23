package spacebros.server.game

import io.vertx.core.http.ServerWebSocket
import spacebros.networking.Messages

// TODO: parallization, buffer / queueing / drain on demand
class ConnectionHub {
    private val connections = hashMapOf<Int, ServerWebSocket>()

    fun register(entityId: Int, websocket: ServerWebSocket) {
        connections[entityId] = websocket
    }

    fun unregister(entityId: Int) {
        connections.remove(entityId)
    }

    fun send(entityId: Int, message: Messages.RootMessage, silent: Boolean = true) {
        val connection = connections[entityId]
        if (connection == null && !silent) throw IllegalArgumentException("entityId ($entityId) not found")
        connection?.writeFinalTextFrame(Messages.encode(message))
    }

    fun broadcast(message: Messages.RootMessage) {
        connections.values.forEach { it.writeFinalTextFrame(Messages.encode(message)) }
    }
}
