package spacebros.server.game

import com.artemis.*
import com.artemis.managers.TagManager
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import spacebros.networking.Messages
import spacebros.server.game.components.MovementComponent
import spacebros.server.game.components.PositionComponent
import spacebros.server.game.components.TileGraphicComponent
import spacebros.server.game.components.TypeComponent
import spacebros.server.game.components.map.EmptyTile
import spacebros.server.game.components.map.TileLayer
import spacebros.server.game.components.map.TiledMap
import spacebros.server.game.systems.MoveSystem
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

class GameVerticle : AbstractVerticle() {
    val connections = arrayListOf<ServerWebSocket>()
    val players     = arrayListOf<Player>()
    val world: World
    var lastTickAt = LocalDateTime.MIN

    val playerArchetype: Archetype

    val connectionHub = ConnectionHub()

    init {
        val config = WorldConfigurationBuilder()
                .with(TagManager())
                .with(MoveSystem(connectionHub))
                .build()
        world = World(config)
        playerArchetype = ArchetypeBuilder()
                .add(TypeComponent::class.java)
                .add(PositionComponent::class.java)
                .add(TileGraphicComponent::class.java)
                .build(world)
    }

    override fun start() {
        bootstrapMap()
        lastTickAt = LocalDateTime.now()
        vertx.setPeriodic(1) {
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
        connectionHub.register(player.entityId, websocket)
        players.add(player)
        synchronizePlayer(player)
        websocket.frameHandler { handleMessage(player, it.textData()) }
        websocket.closeHandler {
            players.remove(player)
            connectionHub.unregister(player.entityId)
        }
    }

    fun createNewPlayer(websocket: ServerWebSocket): Player {
//        val type     = TypeComponent().apply { name = "player" }
//        val position = PositionComponent().apply { x = 5; y = 91; }
//        val graphic  = TileGraphicComponent().apply { graphicFile = "icons/mob/human.png"; tileId = 193 }
//        val entityId = world.createEntity().edit()
//                .add(position)
//                .add(type)
//                .add(graphic)
//                .entityId

        val entityId = world.create(playerArchetype)
        world.getEntity(entityId).apply {
            getComponent(TypeComponent::class.java).apply { name = "player" }
            getComponent(PositionComponent::class.java).apply { x = 5; y = 91 }
            getComponent(TileGraphicComponent::class.java).apply { graphicFile = "icons/mob/human.png"; tileId = 193 }
        }
//        world.getSystem(TagManager::class.java).register("PLAYER", entityId)

        return Player(entityId, websocket)
    }

    fun handleMessage(player: Player, data: String) {
        // TODO: enable player movement around
        println("Got a message from $player: $data")
        val message = Messages.decode(data)
        when(message) {
            is Messages.MoveDirection -> handleMovement(player, message)
        }

    }

    private fun handleMovement(player: Player, message: Messages.MoveDirection) {
        world.edit(player.entityId).add(MovementComponent(message.direction))
    }


    fun synchronizePlayer(player: Player) {
        // TODO: don't sync all entities.
        val entities = world.aspectSubscriptionManager.get(Aspect.all()).entities
        entities.data.forEach { entityId ->
            sendEntity(player.websocket, entityId)
        }
        // Set the camera to their player's position?
        val entity = world.getEntity(player.entityId)
        val pc = entity.getComponent(PositionComponent::class.java)
        val msg = Messages.SetCamera(Messages.Position(pc.x, pc.y))
        connectionHub.broadcast(createEntity(player.entityId))
        connectionHub.send(player.entityId, msg)
    }

    fun sendEntity(websocket: ServerWebSocket, entityId: Int) {
        val message = createEntity(entityId)
        websocket.writeFinalTextFrame(Messages.encode(message))
    }

    private fun createEntity(entityId: Int): Messages.CreateEntity {
        val entity = world.getEntity(entityId)
        val tc = entity.getComponent(TypeComponent::class.java)
        val pc = entity.getComponent(PositionComponent::class.java)
        val tg = entity.getComponent(TileGraphicComponent::class.java)
        val message = Messages.CreateEntity(entityId,
                type = tc.name,
                position = Messages.Position(pc.x, pc.y),
                graphic = Messages.Graphic(tg.tileId, tg.graphicFile)
        )
        return message
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
                            if (tileId != null && tileId > 0) {
                                val flipY = true
                                val tile = tiledMap.getTile(tileId)
                                if (tile !is EmptyTile) {
                                    val localTileId = tile.localTileId
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
