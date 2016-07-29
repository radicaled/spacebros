package spacebros.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import spacebros.game.EntityHub
import spacebros.game.entities.DrawableEntity
import spacebros.game.ui.MainInterface
import spacebros.networking.Messages

class RemotePlayScreen : Screen {
    val cam: OrthographicCamera
    val worldHeight = 100f
    val worldWidth  = 100f

    val rotationSpeed = 0.5f

    val assetManager = AssetManager()

    val vertx: Vertx = Vertx.vertx()

    val entityHub = EntityHub()

    val stage: Stage
    val hud: Stage
    val gameGroup = Group()

    val fillViewport: FillViewport

    val mainInterface: MainInterface
    init {
        // TODO: clean up this entire block, it's a real mess
        val w = Gdx.graphics.width
        val h = Gdx.graphics.height
        // Constructs a new OrthographicCamera, displaying 20 world units at a time.
        cam = OrthographicCamera(20f, 20f * w/h)

        // Position camera in upper left hand corner of worldSprite
        cam.position.set(cam.viewportWidth / 2f, worldHeight - (cam.viewportHeight / 2f), 0f)
        cam.update()

        fillViewport = FillViewport(worldWidth, worldHeight, cam)
        stage = Stage(fillViewport)
        stage.addActor(gameGroup)

        hud = Stage(ScreenViewport())

        Gdx.input.inputProcessor = stage

        mainInterface = MainInterface(hud, assetManager)

        loadRegularAssets()
        mainInterface.setup()
        connectToServerRightNowGoddamnIt()
    }


    private fun loadRegularAssets() {
        assetManager.load("ui/skins/uiskin.json", Skin::class.java)
        assetManager.finishLoading()
    }

    fun loadTileAsset(fileName: String): Texture {
        assetManager.load(fileName, Texture::class.java)
        assetManager.finishLoading()
        return assetManager.get(fileName, Texture::class.java)
    }

    private fun connectToServerRightNowGoddamnIt() {
//        Vertx.vertx().deployVerticle(verticle)
        val opts = HttpClientOptions().setLogActivity(true)
        val client = vertx.createHttpClient(opts)
        client.websocket(8080, "localhost", "/gameStream") { websocket ->
            vertx.eventBus().addInterceptor {
                when(it.message().address()) {
                    "network" -> {
                        val msg = it.message().body()
                        if (msg is String)
                            websocket.writeFinalTextFrame(msg)
                        else
                            println("Unkonwn message type: ${msg.javaClass}")
                    }
                    else -> it.next()
                }
            }
            websocket.frameHandler {
                Gdx.app.postRunnable {
//                    println("Got data: ${it.textData()}")
                    val message = Messages.decode(it.textData())
                    when(message) {
                        is Messages.CreateEntity -> createEntity(message)
                        is Messages.SetCamera -> setCamera(message)
                        is Messages.MoveToPosition -> moveEntity(message)
                        else -> println("Unknown message type: ${it.textData()} (${message.javaClass})")
                    }
                }

            }
        }
    }

    fun queueNetworkMessage(message: Messages.RootMessage) {
        vertx.eventBus().send("network", Messages.encode(message))
    }

    private fun createEntity(message: Messages.CreateEntity) {
        val file = message.graphic.file.trimStart('.', '/')
        val tileId = message.graphic.tileId
        val texture = loadTileAsset(file)
        // TODO: everything 32x32?
        val frameWidth = 32f
        val frameHeight = 32f
        val frameX = ((tileId * frameWidth) % texture.width).toInt()
        val frameY = (Math.floor(((tileId * frameWidth) / texture.width).toDouble()) * frameHeight).toInt()

        val textureRegion = TextureRegion(texture, frameX, frameY, frameWidth.toInt(), frameHeight.toInt())

        val x = message.position.x.toFloat()
        val y = message.position.y.toFloat()

        val entity = DrawableEntity(message.entityId, textureRegion).apply {
            this.x = x
            this.y = y
            this.width = 1f
            this.height = 1f
        }
        gameGroup.addActor(entity)
        entityHub.register(message.entityId, entity)
    }

    private fun setCamera(message: Messages.SetCamera) {
        cam.position.set(message.position.x.toFloat(), message.position.y.toFloat(), 0f)
    }

    private fun moveEntity(message: Messages.MoveToPosition) {
        val entity = entityHub.find(message.entityId)
        entity.x = message.position.x.toFloat()
        entity.y = message.position.y.toFloat()
    }

    fun handleInput(cam: OrthographicCamera) {
//        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//            cam.zoom += 0.02f;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
//            cam.zoom -= 0.02f;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            cam.translate(-3f, 0f, 0f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            cam.translate(3f, 0f, 0f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//            cam.translate(0f, -3f, 0f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//            cam.translate(0f, 3f, 0f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            cam.rotate(-rotationSpeed, 0f, 0f, 1f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
//            cam.rotate(rotationSpeed, 0f, 0f, 1f);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) {
//            println("Camera position: ${cam.position}")
//        }
//        // TODO: 100 == renderSystem.worldWidth
//        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, 100/cam.viewportWidth);
//
//        val effectiveViewportWidth = cam.viewportWidth * cam.zoom;
//        val effectiveViewportHeight = cam.viewportHeight * cam.zoom;
//
//        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
//        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            queueNetworkMessage(Messages.MoveDirection(Messages.Direction.WEST))
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            queueNetworkMessage(Messages.MoveDirection(Messages.Direction.EAST))
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            queueNetworkMessage(Messages.MoveDirection(Messages.Direction.SOUTH))
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            queueNetworkMessage(Messages.MoveDirection(Messages.Direction.NORTH))
        }
    }
    override fun show() {

    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {
        cam.viewportWidth = 20f
        cam.viewportHeight = 20f * height/width

        cam.update()

//        fillViewport.update(width, height, true)
    }

    override fun hide() {

    }

    override fun render(delta: Float) {
//        handleInput(stage.camera as OrthographicCamera)
        handleInput(cam)
//        cam.position.set(cam.position.x + 1, cam.position.y, cam.position.z)

        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        mainInterface.update()

        stage.act(delta)
        hud.act(delta)

        stage.draw()
        hud.draw()

    }

    override fun resume() {

    }

    override fun dispose() {
        stage.dispose()
        hud.dispose()
    }
}
