package spacebros.server.game

import io.vertx.core.http.ServerWebSocket
import spacebros.networking.Messages

// TODO: parallization, buffer / queueing / drain on demand
class ConnectionHub {
    private val connections = hashMapOf<Int, GameConnection>()

    fun register(entityId: Int, gameConnection: GameConnection) {
        connections[entityId] = gameConnection
    }

    fun unregister(entityId: Int) {
        connections.remove(entityId)
    }

    fun send(entityId: Int, message: Messages.RootMessage, silent: Boolean = true) {
        val connection = connections[entityId]
        if (connection == null && !silent) throw IllegalArgumentException("entityId ($entityId) not found")
        connection?.sendData(Messages.encode(message))
    }

    fun broadcast(message: Messages.RootMessage) {
        connections.values.forEach { it.sendData(Messages.encode(message)) }
    }
}
