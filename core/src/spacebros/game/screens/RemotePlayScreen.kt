package spacebros.game.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.WebSocketFrame
import io.vertx.core.json.JsonObject
import spacebros.game.components.PositionComponent
import spacebros.game.components.RenderableComponent
import spacebros.game.systems.RenderSystem
import spacebros.networking.Messages

class RemotePlayScreen : Screen {
    val cam: OrthographicCamera

    val rotationSpeed = 0.5f
    val verticle: ClientVerticle

    val world: World

    val assetManager = AssetManager()

    init {
        val config = WorldConfigurationBuilder()
                .with(RenderSystem())
                .build()
        world = World(config)

        cam = world.getSystem(RenderSystem::class.java).camera

        verticle = ClientVerticle()
        connectToServerRightNowGoddamnIt()
    }

    fun loadTileAsset(fileName: String): Texture {
        assetManager.load(fileName, Texture::class.java)
        assetManager.finishLoading()
        return assetManager.get(fileName, Texture::class.java)
    }

    private fun connectToServerRightNowGoddamnIt() {
//        Vertx.vertx().deployVerticle(verticle)
        val opts = HttpClientOptions().setLogActivity(true)
        val client = Vertx.vertx().createHttpClient(opts)
        client.websocket(8080, "localhost", "/gameStream") { websocket ->
            websocket.frameHandler {
                Gdx.app.postRunnable {
                     println("Got data: ${it.textData()}")
                    val message = Messages.decode(it.textData())
                    when(message) {
                        is Messages.CreateEntity -> createEntity(message)
                        is Messages.SetCamera -> setCamera(message)
                        else -> println("Unknown message type: ${it.textData()} (${message.javaClass})")
                    }
                }

            }
        }
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

        if (file == "icons/obj/closet.png") {
            println("Localized tile ID: ${tileId}")
            println("Pixel: ${frameX}, ${frameY}")
            println("Frame location: ${frameX / 32f}, ${frameY / 32f}")
        }

        val rc = RenderableComponent().apply { this.textureRegion = textureRegion }
        val pc = PositionComponent().apply { this.vector = Vector2(x, y) }
        world.createEntity().edit()
                .add(rc)
                .add(pc)
    }

    private fun setCamera(message: Messages.SetCamera) {
        cam.position.set(message.position.x.toFloat(), message.position.y.toFloat(), 0f)
    }

    fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cam.zoom += 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            cam.zoom -= 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cam.translate(-3f, 0f, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cam.translate(3f, 0f, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cam.translate(0f, -3f, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cam.translate(0f, 3f, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cam.rotate(-rotationSpeed, 0f, 0f, 1f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            cam.rotate(rotationSpeed, 0f, 0f, 1f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) {
            println("Camera position: ${cam.position}")
        }
        // TODO: 100 == renderSystem.worldWidth
        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, 100/cam.viewportWidth);

        val effectiveViewportWidth = cam.viewportWidth * cam.zoom;
        val effectiveViewportHeight = cam.viewportHeight * cam.zoom;

        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);
    }
    override fun show() {

    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {
        cam.viewportWidth = 20f
        cam.viewportHeight = 20f * height/width
        cam.update()
    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        handleInput()
        world.delta = delta
        world.process()
        cam.update()
    }

    override fun resume() {

    }

    override fun dispose() {

    }
}

// TODO: don't use me yet
class ClientVerticle : AbstractVerticle() {
    override fun start() {
        val opts = HttpClientOptions().setLogActivity(true)
        val client = vertx.createHttpClient(opts)
        client.websocket(8080, "localhost", "/gameStream") { websocket ->
            websocket.frameHandler {
                println("Got data: $it")
            }
        }
    }
}
