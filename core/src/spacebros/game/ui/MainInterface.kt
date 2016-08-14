package spacebros.game.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.TextArea
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import rx.lang.kotlin.PublishSubject

class MainInterface(val stage: Stage, val assetManager: AssetManager) {
    lateinit var cameraPosition: Label
    lateinit var screenInfo: Label

    lateinit var textArea: TextArea

    val textFieldSubmissions = PublishSubject<String>()
    // TODO: MASSIVE, MASSIVE CLEAN-up
    fun setup() {
        val skin = assetManager.get("ui/skins/uiskin.json", Skin::class.java)
        val mainTable = Table().apply {
            top()
            setFillParent(true)
            debug = true
            zIndex = 10
        }

        val table = Table().apply {
            top().right()
            debug = true
//            zIndex = 10
        }
        cameraPosition = Label("...", skin)
        screenInfo = Label("...", skin)



        val textField = TextField("", skin)
        textField.addListener(object : InputListener() {
            override fun keyTyped(event: InputEvent, character: Char): Boolean {
                return when(event.keyCode) {
                    Input.Keys.ESCAPE -> {
                        stage.keyboardFocus = null
                        true
                    }
                    Input.Keys.ENTER -> {
                        textFieldSubmissions.onNext(textField.text)
                        textField.text = ""
                        true

                    }
                    else -> false
                }
            }
        })

        textArea  = TextArea("Player 1 has arrived", skin).apply {
            width = 50f
            height = 50f
        }
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

    fun addToMessageLog(text: String) {
        textArea.appendText(text)
    }

    fun update() {
        cameraPosition.setText(stage.camera.position.toString())
        screenInfo.setText("${stage.viewport.screenWidth}, ${stage.viewport.screenHeight}")
    }
}
