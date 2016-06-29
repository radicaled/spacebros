package spacebros.game.systems

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import spacebros.game.components.PositionComponent
import spacebros.game.components.RenderableComponent

class RenderSystem : IteratingSystem(aspects) {
    companion object {
        val aspects = Aspect.all(RenderableComponent::class.java,
                PositionComponent::class.java)
    }

    lateinit var renderMapper: ComponentMapper<RenderableComponent>
    lateinit var posMapper: ComponentMapper<PositionComponent>

    val batch = SpriteBatch()
    val worldSprite = Sprite()
    val camera = OrthographicCamera()

    val worldHeight = 100f
    val worldWidth  = 100f

    init {
        val w = Gdx.graphics.width;
        val h = Gdx.graphics.height;

        worldSprite.setPosition(0f, 0f)
        worldSprite.setSize(worldWidth, worldHeight)

        // Constructs a new OrthographicCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio.
        camera.setToOrtho(false, 30f, 30f * (h / w))
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f);
        camera.update()
    }

    override fun process(entityId: Int) {
        val renderable = renderMapper.get(entityId)
        val pos = posMapper.get(entityId)
        val vector = pos.vector
        if (vector != null) // TODO: make vectors non-nullable
            batch.draw(renderable.textureRegion, vector.x, vector.y, 1f, 1f) // everything is 1 meter?

    }

    override fun end() {
        super.end()
        batch.end()
    }

    override fun begin() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined

//        println("${camera.position}")

        batch.begin()
        super.begin()
    }
}
