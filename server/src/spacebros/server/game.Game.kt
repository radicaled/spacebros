package spacebros.server

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalField

class GameVerticle : AbstractVerticle() {
    val connections = arrayListOf<ServerWebSocket>()
    val world: World
    var lastTickAt = LocalDateTime.MIN

    init {
        val config = WorldConfigurationBuilder()
                .build()
        world = World(config)
    }

    override fun start() {
        lastTickAt = LocalDateTime.now()
        vertx.setPeriodic(250) {
            val delta = Duration.between(lastTickAt, LocalDateTime.now()).toMillis()
            tick(delta.toFloat())
            lastTickAt = LocalDateTime.now()
        }
    }

    fun tick(delta: Float) {
        world.delta = delta
        world.process()
    }

    fun acceptRemoteConnection(websocket: ServerWebSocket) {
        connections.add(websocket)
        websocket.frameHandler { handleMessage(websocket, JsonObject(it.textData())) }
        websocket.closeHandler {
            connections.remove(websocket)
        }
    }

    fun handleMessage(websocket: ServerWebSocket, data: JsonObject) {
    }
}
