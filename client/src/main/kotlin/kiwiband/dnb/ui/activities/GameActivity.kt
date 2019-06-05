package kiwiband.dnb.ui.activities

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kiwiband.dnb.Game
import kiwiband.dnb.events.EventGameOver
import kiwiband.dnb.events.EventKeyPress
import kiwiband.dnb.events.EventMove
import kiwiband.dnb.events.EventTick
import kiwiband.dnb.map.MapSaver
import kiwiband.dnb.math.Vec2
import kiwiband.dnb.ui.App
import kiwiband.dnb.ui.AppContext
import kiwiband.dnb.ui.views.InfoView
import kiwiband.dnb.ui.views.MapView
import kiwiband.dnb.ui.views.PlayerView
import kiwiband.dnb.ui.views.View
import kiwiband.dnb.ui.views.layout.BoxLayout
import kiwiband.dnb.ui.views.layout.HorizontalLayout
import kiwiband.dnb.ui.views.layout.VerticalLayout

class GameActivity(private val game: Game,
                   context: AppContext,
                   callback: (Boolean) -> Unit): Activity<Boolean>(context, callback) {

    private val mapSaver = MapSaver()
    private val mapFile = "./maps/saved_map.dnb"

    override fun createRootView(): View {
        val gameRootView = HorizontalLayout(App.SCREEN_WIDTH, App.SCREEN_HEIGHT)

        val mapView = MapView(game.map, 48, 22)
        val playerView = PlayerView(game.player, 28, 10)
        val infoView = InfoView(28, 10)

        gameRootView.addChild(BoxLayout(mapView))

        val sidebar = VerticalLayout(30, 24)
        sidebar.addChild(BoxLayout(infoView))
        sidebar.addChild(BoxLayout(playerView))

        gameRootView.addChild(sidebar)
        return gameRootView
    }

    private fun handleMoveKeys(keyStroke: KeyStroke) {
        if (keyStroke.keyType != KeyType.Character) return
        when (keyStroke.character) {
            'w', 'ц' -> Vec2(0, -1)
            'a', 'ф' -> Vec2(-1, 0)
            's', 'ы' -> Vec2(0, 1)
            'd', 'в' -> Vec2(1, 0)
            else -> null
        }?.also {
            EventMove.dispatcher.run(EventMove(it))
            tick()
        }
    }

    private fun tick() {
        EventTick.dispatcher.run(EventTick())
        drawScene()
    }

    private fun saveMap(game: Game) {
        mapSaver.saveToFile(game.map, mapFile)
    }

    private fun deleteMap() {
        mapSaver.deleteFile(mapFile)
    }

    private fun openInventory(game: Game) {
        InventoryActivity(game.player, context, {}).start()
    }

    override fun onStart() {
        drawScene()

        EventKeyPress.dispatcher.addHandler {
            if (it.key.keyType == KeyType.Escape) {
                EventGameOver.dispatcher.run(EventGameOver())
            }
        }

        EventKeyPress.dispatcher.addHandler { handleMoveKeys(it.key) }

        EventKeyPress.dispatcher.addHandler {
            if (it.key.keyType == KeyType.Character) {
                when (it.key.character) {
                    'i', 'ш' -> openInventory(game)
                }
            }
        }

        EventGameOver.dispatcher.addHandler {
            val isDead = game.player.isDead()
            if (isDead) {
                deleteMap()
            } else {
                saveMap(game)
            }
            game.endGame()
            finish(isDead)
        }

        game.startGame()
    }

    override fun onFinish(result: Boolean) {

    }
}