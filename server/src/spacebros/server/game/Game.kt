package spacebros.server.game

import com.artemis.*
import com.artemis.managers.TagManager
import com.artemis.utils.Bag
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import spacebros.networking.Messages
import spacebros.server.game.behaviors.Behavior
import spacebros.server.game.behaviors.BehaviorRegistry
import spacebros.server.game.behaviors.makeBehaviorRegistry
import spacebros.server.game.components.*
import spacebros.server.game.components.map.EmptyTile
import spacebros.server.game.components.map.TileLayer
import spacebros.server.game.components.map.TiledMap
import spacebros.server.game.entities.ArchetypeRegistry
import spacebros.server.game.entities.makeRegistry
import spacebros.server.game.systems.BehaviorSystem
import spacebros.server.game.systems.CollisionSystem
import spacebros.server.game.systems.MoveSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class GameVerticle : AbstractVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    val connections = arrayListOf<ServerWebSocket>()
    val players     = arrayListOf<Player>()
    val world: World
    var lastTickAt = LocalDateTime.MIN

    val archetypeRegistry: ArchetypeRegistry
    val behaviorRegistry: BehaviorRegistry
    val connectionHub = ConnectionHub()
    val intentQueue = IntentQueue()
    val clientSerializer = ClientSerializer()

    init {
        val config = WorldConfigurationBuilder()
                .with(TagManager())
                .with(CollisionSystem())
                .with(MoveSystem(connectionHub))
                .with(BehaviorSystem(intentQueue))
                .build()
        config
                .register("ConnectionHub", connectionHub)
                .register("ClientSerializer", clientSerializer)
        world = World(config)
        archetypeRegistry = makeRegistry(world)
        behaviorRegistry  = makeBehaviorRegistry(world)
    }

    override fun start() {
        vertx.exceptionHandler {
            vertx.close {
                logger.error("Terminating verticle...")
                System.exit(-1)
            }
        }
//        bootstrapMap()
        bootstrapCustomMap()
//        saveMap()
        lastTickAt = LocalDateTime.now()
        vertx.setPeriodic(1) {
            val delta = Duration.between(lastTickAt, LocalDateTime.now()).toMillis()
            tick(delta.toFloat())
            lastTickAt = LocalDateTime.now()
        }
        logger.info("Server ready.")
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
            is Messages.SynchronizeRequest -> handleSynchronize(player, message)
            is Messages.MoveDirection -> handleMovement(player, message)
            is Messages.TextMessage -> handleTextMessage(player, message)
            is Messages.Interaction -> handleInteractionMessage(player, message)
        }

    }

    private fun handleInteractionMessage(player: Player, message: Messages.Interaction) {
        val intent = Intent(player.entityId, message.entityId, message.action)
        intentQueue.append(intent)
    }

    private fun handleTextMessage(player: Player, message: Messages.TextMessage) {
        when(message.textType) {
            Messages.TextType.SPEAK -> {
                val entity = world.getEntity(player.entityId)
                val name = entity.getComponent(NameComponent::class.java).name
                val text = "$name: ${message.message}"
                val textMessage = Messages.TextMessage(text, Messages.TextType.SPEAK).apply {
                    entityId = player.entityId
                }
                connectionHub.broadcast(textMessage)
            }
            else -> {
                println("Player $player send an unsupported message.")
            }
        }
    }

    private fun handleMovement(player: Player, message: Messages.MoveDirection) {
        world.edit(player.entityId).add(MovementComponent(message.direction))
    }

    private fun handleSynchronize(player: Player, message: Messages.SynchronizeRequest) {
        synchronizePlayer(player)
    }

    private fun handleLogin(player: Player, message: Messages.Login) {
        // TODO: authentication
        world.getEntity(player.entityId).apply {
            getComponent(NameComponent::class.java).apply { name = message.playerName }
        }
        connectionHub.send(player.entityId, Messages.LoginSuccess("Welcome back, ${message.playerName}"))
        connectionHub.broadcast(Messages.TextMessage("${message.playerName} has joined!", Messages.TextType.MESSAGE))
    }


    fun synchronizePlayer(player: Player) {
        // TODO: only sync entities in sight of player
        val entityAspects = Aspect.all(VisibilityComponent::class.java)
        val entities = world.aspectSubscriptionManager.get(entityAspects).entities
        entities.active().forEach {
            val entityId = entities[it]
            sendEntity(player.gameConnection, entityId)
        }

        // Set the camera to their player's position?
        val entity = world.getEntity(player.entityId)
        val pc = entity.getComponent(PositionComponent::class.java)
        val cameraMessage = Messages.SetCamera(Messages.Position(pc.x, pc.y, pc.z))
        val playerMessage = Messages.SetPlayerEntity(player.entityId)

        connectionHub.send(player.entityId, cameraMessage)
        connectionHub.send(player.entityId, playerMessage)
    }

    fun sendEntity(gameConnection: GameConnection, entityId: Int) {
        val message = createEntity(entityId)
        gameConnection.sendData(Messages.encode(message))
    }

    private fun createEntity(entityId: Int): Messages.CreateEntity {
        val entity = world.getEntity(entityId)
        val bag = Bag<Component>()
        entity.getComponents(bag)

        val message = Messages.CreateEntity(
                entityId,
                clientSerializer.serialize(bag)
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
        entities.active().forEach { entityId ->
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
            val behaviors = arrayListOf<Behavior>()
            val archetype = if (archetypeRegistry.has(mapEntity.type!!)) {
                archetypeRegistry.get(mapEntity.type!!)
            } else {
                archetypeRegistry.get("visual")
            }
            // TODO: map -> filter -> ?
            // what about error reporting (eg no matching behavior?)
            mapEntity.behaviors.forEach { behaviorName ->
                if (behaviorRegistry.has(behaviorName))
                    behaviors.add(behaviorRegistry.get(behaviorName))
            }

            // TODO: must be some way to clean this up
            val entity = world.createEntity(archetype)
            if (behaviors.size > 0) {
                entity.edit().add(BehaviorComponent(behaviors))
            }
            val tc = entity.getComponent(TypeComponent::class.java)
            val pc = entity.getComponent(PositionComponent::class.java)
            val tg = entity.getComponent(TileGraphicComponent::class.java)
            val nc = entity.getComponent(NameComponent::class.java)

            tc.name = mapEntity.type!!
            nc.name = mapEntity.type!!
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
