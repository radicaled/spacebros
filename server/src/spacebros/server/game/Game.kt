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
import spacebros.server.game.components.*
import spacebros.server.game.components.map.EmptyTile
import spacebros.server.game.components.map.TileLayer
import spacebros.server.game.components.map.TiledMap
import spacebros.server.game.entities.ArchetypeRegistry
import spacebros.server.game.entities.makeRegistry
import spacebros.server.game.systems.CollisionSystem
import spacebros.server.game.systems.MoveSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime

class GameVerticle : AbstractVerticle() {
    val connections = arrayListOf<ServerWebSocket>()
    val players     = arrayListOf<Player>()
    val world: World
    var lastTickAt = LocalDateTime.MIN

    val archetypeRegistry: ArchetypeRegistry

    val connectionHub = ConnectionHub()

    init {
        val config = WorldConfigurationBuilder()
                .with(TagManager())
                .with(CollisionSystem())
                .with(MoveSystem(connectionHub))
                .build()
        world = World(config)
        archetypeRegistry = makeRegistry(world)
    }

    override fun start() {
//        bootstrapMap()
        bootstrapCustomMap()
//        saveMap()
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

    fun acceptRemoteConnection(gameConnection: GameConnection) {
        val player = createNewPlayer(gameConnection)
        connectionHub.register(player.entityId, gameConnection)
        players.add(player)

        gameConnection.dataHandler { handleMessage(player, it) }
        gameConnection.closeHandler {
            world.delete(player.entityId)
            players.remove(player)
            connectionHub.unregister(player.entityId)
            connectionHub.broadcast(Messages.DeleteEntity(player.entityId))

        }
    }

    fun createNewPlayer(gameConnection: GameConnection): Player {
        val entityId = world.create(archetypeRegistry.get("player"))
        world.getEntity(entityId).apply {
            getComponent(TypeComponent::class.java).apply { name = "player" }
            getComponent(PositionComponent::class.java).apply { x = 5; y = 8; z = 1 }
            getComponent(TileGraphicComponent::class.java).apply { graphicFile = "icons/mob/human.png"; tileId = 193 }
        }

        return Player(entityId, gameConnection)
    }

    fun handleMessage(player: Player, data: String) {
        // TODO: enable player movement around
        println("Got a message from $player: $data")
        val message = Messages.decode(data)
        when(message) {
            is Messages.Login -> handleLogin(player, message)
            is Messages.MoveDirection -> handleMovement(player, message)
            is Messages.TextMessage -> handleTextMessage(player, message)
        }

    }

    private fun handleTextMessage(player: Player, message: Messages.TextMessage) {
        val entity = world.getEntity(player.entityId)
        val name = entity.getComponent(NameComponent::class.java).name
        val text = "$name: ${message.message}"
        connectionHub.broadcast(Messages.TextMessage(text))
    }

    private fun handleMovement(player: Player, message: Messages.MoveDirection) {
        world.edit(player.entityId).add(MovementComponent(message.direction))
    }

    private fun handleLogin(player: Player, message: Messages.Login) {
        world.getEntity(player.entityId).apply {
            getComponent(NameComponent::class.java).apply { name = message.playerName }
        }
        synchronizePlayer(player)
        connectionHub.broadcast(Messages.TextMessage("${message.playerName} has joined!"))
    }


    fun synchronizePlayer(player: Player) {
        // TODO: don't sync all entities.
        val entities = world.aspectSubscriptionManager.get(Aspect.all()).entities
        entities.data.forEach { entityId ->
            sendEntity(player.gameConnection, entityId)
        }
        // Set the camera to their player's position?
        val entity = world.getEntity(player.entityId)
        val pc = entity.getComponent(PositionComponent::class.java)
        val msg = Messages.SetCamera(Messages.Position(pc.x, pc.y, pc.z))
        connectionHub.broadcast(createEntity(player.entityId))
        connectionHub.send(player.entityId, msg)
    }

    fun sendEntity(gameConnection: GameConnection, entityId: Int) {
        val message = createEntity(entityId)
        gameConnection.sendData(Messages.encode(message))
    }

    private fun createEntity(entityId: Int): Messages.CreateEntity {
        val entity = world.getEntity(entityId)
        val tc = entity.getComponent(TypeComponent::class.java)
        val pc = entity.getComponent(PositionComponent::class.java)
        val tg = entity.getComponent(TileGraphicComponent::class.java)
        val message = Messages.CreateEntity(entityId,
                type = tc.name,
                position = Messages.Position(pc.x, pc.y, pc.z),
                graphic = Messages.Graphic(tg.tileId, tg.graphicFile)
        )
        return message
    }

    fun saveMap() {
        world.process()
        val mapper = jacksonObjectMapper()
                .enableDefaultTyping()
                .registerKotlinModule().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
        val map = Map()
        val entities = world.aspectSubscriptionManager.get(Aspect.all()).entities
        val filenames = hashSetOf<String>()
        entities.data.forEach { entityId ->
            val entity = world.getEntity(entityId)
            val tc = entity.getComponent(TypeComponent::class.java)
            val pc = entity.getComponent(PositionComponent::class.java)
            val tg = entity.getComponent(TileGraphicComponent::class.java)

            val fileName = Paths.get(tg.graphicFile).fileName.toString()
            filenames.add(fileName)
            map.entities.add(MapEntity().apply {
                type = fileName.substring(0, fileName.lastIndexOf("."))
                position = MapPosition().apply {
                    x = pc.x
                    y = pc.y
                    z = pc.z
                }
                graphic = tg.tileId
            })
        }
        val path = Paths.get("/home/arron/Projects/spacebros/server/assets/saved_map.json")
        val writer = Files.newBufferedWriter(path)
//        val fileContents = mapper.writeValueAsString(map)
        mapper.writeValue(writer, map)
    }

    fun bootstrapCustomMap() {
        val mapLocation = "/home/arron/Projects/spacebros/server/assets/map.json"
        val mapper = jacksonObjectMapper()
                .enableDefaultTyping()
                .registerKotlinModule().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
        val fileData = File(mapLocation).readText()
        val map = mapper.readValue(fileData, Map::class.java)
        // TODO: clean up once formalized
        map.entities.forEach { mapEntity ->
            val archetype = if (archetypeRegistry.has(mapEntity.type!!)) {
                archetypeRegistry.get(mapEntity.type!!)
            } else {
                archetypeRegistry.get("visual")
            }

            // TODO: must be some way to clean this up
            val entity = world.createEntity(archetype)
            val tc = entity.getComponent(TypeComponent::class.java)
            val pc = entity.getComponent(PositionComponent::class.java)
            val tg = entity.getComponent(TileGraphicComponent::class.java)

            tc.name = mapEntity.type!!
            pc.apply {
                this.x = mapEntity.position?.x!!
                this.y = mapEntity.position?.y!!
            }
            tg.apply {
                this.tileId = mapEntity.graphic!!
                this.graphicFile = mapEntity.type!!
            }
        }

    }

    fun bootstrapMap() {
        val mapLocation = "/home/arron/Projects/spacebros/core/assets/maps/shit_station-1.json"
        val fileData = File(mapLocation).readText()
        val json = JsonObject(fileData)
        val tiledMap = TiledMap.parse(json)

        tiledMap.layers.forEachIndexed { index, layer ->
            when(layer) {
                is TileLayer ->
                    (0..layer.height - 1).forEach { y ->
                        (0..layer.width - 1).forEach { x ->
                            val tileId = layer.getCell(x, y)
                            if (tileId != null && tileId > 0) {
                                val flipY = false
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
                                        .add(PositionComponent().apply {
                                            this.x = x
                                            this.y = actualValueOfYBecauseStupidProgrammingThings
                                            this.z = index
                                        })
                                        .add(TileGraphicComponent().apply { this.tileId = localTileId; this.graphicFile = tile.tileset.image })
                                }
                            }
                        }
                    }
            }
        }
    }
}
