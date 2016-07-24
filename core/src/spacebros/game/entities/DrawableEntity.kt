package spacebros.game.entities

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class DrawableEntity(val entityId: Int, val textureRegion: TextureRegion) : Entity(entityId) {
    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(textureRegion, x, y,
                originX, originY,
                width, height,
                scaleX, scaleY, rotation)
    }
}
