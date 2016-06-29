package spacebros.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import spacebros.game.screens.PlayScreen

class SpacebrosGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var img: Texture

    override fun create() {
        setScreen(PlayScreen())
    }

    override fun render() {
        super.render()
    }
}
