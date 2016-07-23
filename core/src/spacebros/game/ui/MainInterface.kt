package spacebros.game.ui

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField

class MainInterface(val stage: Stage, val assetManager: AssetManager) {
    fun setup() {
        val skin = assetManager.get("ui/skins/uiskin.json", Skin::class.java)
        val table = Table()
        table.setFillParent(true)
        table.debug = true

        val textField = TextField("Hello, World", skin)
        table.add(textField)

        stage.addActor(table)
    }
}
