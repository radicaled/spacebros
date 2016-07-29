package spacebros.game.ui

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*

class MainInterface(val stage: Stage, val assetManager: AssetManager) {
    lateinit var cameraPosition: Label
    lateinit var screenInfo: Label
    fun setup() {
        val skin = assetManager.get("ui/skins/uiskin.json", Skin::class.java)
        val mainTable = Table().apply {
            top()
            setFillParent(true)
            debug = true
        }

        val table = Table().apply {
            top().right()
            debug = true
//            zIndex = 10
        }
        cameraPosition = Label("...", skin)
        screenInfo = Label("...", skin)

        val textField = TextField("Hello, World", skin)
        val textArea  = TextArea("Player 1 has arrived", skin)
//        table.add(textField)
        table.add(cameraPosition)
        table.row()
        table.add(screenInfo)

        mainTable.add(table)
                .expandX()
                .top().right()
        mainTable.row()
        mainTable.add(Table().apply {
            add(textArea)
            row()
            add(textField)
        }).expandY().bottom().left()
        stage.addActor(mainTable)
    }

    fun update() {
        cameraPosition.setText(stage.camera.position.toString())
        screenInfo.setText("${stage.viewport.screenWidth}, ${stage.viewport.screenHeight}")
    }
}
