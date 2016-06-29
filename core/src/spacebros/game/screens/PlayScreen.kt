package spacebros.game.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import spacebros.game.components.PositionComponent
import spacebros.game.components.RenderableComponent
import spacebros.game.systems.RenderSystem

class PlayScreen : Screen {
    val world: World
    lateinit var cam: OrthographicCamera

    init {
        val config = WorldConfigurationBuilder()
            .with(RenderSystem())
            .build()
        world = World(config)

        loadMap()
        world.getSystem(RenderSystem::class.java).let {
            cam = it.camera
        }
    }
    override fun show() {

    }

    override fun pause() {
    }

    override fun resize(width: Int, height: Int) {
        cam.viewportWidth = 30f;
        cam.viewportHeight = 30f * height/width;
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
    val rotationSpeed = 0.5f

    fun handleInput() {
        if (input.isKeyPressed(Input.Keys.A)) {
            cam.zoom += 0.02f;
        }
        if (input.isKeyPressed(Input.Keys.Q)) {
            cam.zoom -= 0.02f;
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            cam.translate(-3f, 0f, 0f);
        }
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            cam.translate(3f, 0f, 0f);
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            cam.translate(0f, -3f, 0f);
        }
        if (input.isKeyPressed(Input.Keys.UP)) {
            cam.translate(0f, 3f, 0f);
        }
        if (input.isKeyPressed(Input.Keys.W)) {
            cam.rotate(-rotationSpeed, 0f, 0f, 1f);
        }
        if (input.isKeyPressed(Input.Keys.E)) {
            cam.rotate(rotationSpeed, 0f, 0f, 1f);
        }
        // TODO: 100 == renderSystem.worldWidth
        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, 100/cam.viewportWidth);

        val effectiveViewportWidth = cam.viewportWidth * cam.zoom;
        val effectiveViewportHeight = cam.viewportHeight * cam.zoom;

        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f);
        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f);

    }

    fun loadMap() {
        val map = TmxMapLoader().load("maps/shit_station-1.tmx")
        val tileWidth = 32
        val tileHeight = 32

        map.layers.forEach { layer ->
            when(layer) {
                is TiledMapTileLayer ->
                    (0..layer.height).forEach { y ->
                        (0..layer.width).forEach { x ->
                            val cell = layer.getCell(x, y)
                            if (cell != null)
                                makeEntity(cell.tile, x,y)

                        }
                    }
            }
        }
    }

    fun makeEntity(mapTile: TiledMapTile, x: Int, y: Int) {
        val tileWidth = 1
        val tileHeight = 1
        val vector = Vector2((x * tileWidth).toFloat(), (y * tileHeight).toFloat())
        val graphic = mapTile.textureRegion
        val rc = RenderableComponent().apply { textureRegion = graphic }
        val pc = PositionComponent().apply { this.vector = vector }
        world.createEntity().edit()
                .add(rc)
                .add(pc)
    }

}
