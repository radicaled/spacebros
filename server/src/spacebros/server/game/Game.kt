package spacebros.server.game

import com.artemis.Aspect
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import spacebros.server.game.components.PositionComponent
import spacebros.server.game.components.TypeComponent
import spacebros.server.game.components.map.TileLayer
import spacebros.server.game.components.map.TiledMap
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.zip.Inflater

class GameVerticle : AbstractVerticle() {
    val connections = arrayListOf<ServerWebSocket>()
    val players     = arrayListOf<Player>()
    val world: World
    var lastTickAt = LocalDateTime.MIN

    init {
        val config = WorldConfigurationBuilder()
                .build()
        world = World(config)
    }

    override fun start() {
        bootstrapMap()
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
        val player = createNewPlayer(websocket)
        connections.add(websocket)
        players.add(player)
        synchronizePlayer(player)
        websocket.frameHandler { handleMessage(player, JsonObject(it.textData())) }
        websocket.closeHandler {
            players.remove(player)
            connections.remove(websocket)
        }
    }

    fun createNewPlayer(websocket: ServerWebSocket): Player {
        val type     = TypeComponent().apply { name = "player" }
        val position = PositionComponent().apply { x = 5; y = 7; }
        val entityId = world.createEntity().edit()
                .add(position)
                .add(type)
                .entityId
        return Player(entityId, websocket)
    }

    fun handleMessage(player: Player, data: JsonObject) {
        // TODO: enable player movement around
        println("Got a message from $player: $data")
    }

    fun synchronizePlayer(player: Player) {
        // TODO: don't sync all entities.
        val entities = world.aspectSubscriptionManager.get(Aspect.all()).entities
        entities.data.forEach { entityId ->
            sendEntity(player.websocket, entityId)
        }
    }

    fun sendEntity(websocket: ServerWebSocket, entityId: Int) {
        val entity = world.getEntity(entityId)
        val tc = entity.getComponent(TypeComponent::class.java)
        val pc = entity.getComponent(PositionComponent::class.java)
        val json = JsonObject(mapOf(
                "type" to tc.name,
                "position" to mapOf("x" to pc.x, "y" to pc.y)

        ))
        websocket.writeFinalTextFrame(json.encodePrettily())
    }

    fun bootstrapMap() {
        val mapLocation = "/home/arron/Projects/spacebros/core/assets/maps/shit_station-1.json"
        val fileData = File(mapLocation).readText()
        val json = JsonObject(fileData)
        val tiledMap = TiledMap.parse(json)

        tiledMap.layers.forEach { layer ->
            when(layer) {
                is TileLayer ->
                    (0..layer.height - 1).forEach { y ->
                        (0..layer.width - 1).forEach { x ->
                            val tile = layer.getTile(x, y)
                            if (tile != null && tile > 0)
                                println("Discovered ${tile}")

                        }
                    }
            }
        }
    }
}
