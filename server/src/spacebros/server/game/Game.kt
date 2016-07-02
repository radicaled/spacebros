package spacebros.server.game

import com.artemis.Aspect
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import spacebros.server.game.components.PositionComponent
import spacebros.server.game.components.TileGraphicComponent
import spacebros.server.game.components.TypeComponent
import spacebros.server.game.components.map.EmptyTile
import spacebros.server.game.components.map.TileLayer
import spacebros.server.game.components.map.TiledMap
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

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
        val graphic  = TileGraphicComponent().apply { graphicFile = "icons/mob/human.png"; tileId = 193 }
        val entityId = world.createEntity().edit()
                .add(position)
                .add(type)
                .add(graphic)
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
        val tg = entity.getComponent(TileGraphicComponent::class.java)
        val json = JsonObject(mapOf(
                "type" to tc.name,
                "position" to mapOf("x" to pc.x, "y" to pc.y),
                "graphic" to mapOf("tileId" to tg.tileId, "file" to tg.graphicFile)

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
                            val tileId = layer.getCell(x, y)
                            if (x == 19 && y == 2 && layer.name == "Items") {
                                println("The tileGid at $x,$y is: ${tileId}")
                            }
                            if (tileId != null && tileId > 0) {
                                val flipY = true
                                val tile = tiledMap.getTile(tileId)
                                if (tile !is EmptyTile) {
                                    val localTileId = tile.localTileId
                                    if (tile.tileset.name == "Closets") {
                                        println("Found tile: (gid) ${tile.gid} (local) ${localTileId}")
                                        println("$x, $y")
                                    }
                                    // Ternary operators? What are those?!
                                    // A man just needs 200 bytes of RAM and his bootstraps, lemme tell you.
                                    val actualValueOfYBecauseStupidProgrammingThings = if (flipY) {
                                        layer.height - 1 - y
                                    } else { y }
                                    world.createEntity().edit()
                                        .add(TypeComponent())
                                        .add(PositionComponent().apply { this.x = x; this.y = actualValueOfYBecauseStupidProgrammingThings })
                                        .add(TileGraphicComponent().apply { this.tileId = localTileId; this.graphicFile = tile.tileset.image })
                                }
                            }
                        }
                    }
            }
        }
    }
}
