package spacebros.game.ui

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table

class MainInterface(val stage: Stage, val assetManager: AssetManager) {
    lateinit var cameraPosition: Label
    lateinit var screenInfo: Label
    fun setup() {
        val uiGroup = Group()
        val skin = assetManager.get("ui/skins/uiskin.json", Skin::class.java)
        val table = Table()
        table.setFillParent(true)
        table.debug = true
        table.right().top()


        table.zIndex = 10

        cameraPosition = Label("...", skin)
        screenInfo = Label("...", skin)

//        val textField = TextField("Hello, World", skin)
//        table.add(textField)
        table.add(cameraPosition)
        table.row()
        table.add(screenInfo)

//        uiGroup.addActor(table)
//        stage.addActor(uiGroup)
        stage.addActor(table)
    }

    fun update() {
        cameraPosition.setText(stage.camera.position.toString())
        screenInfo.setText("${stage.viewport.screenWidth}, ${stage.viewport.screenHeight}")
    }
}
