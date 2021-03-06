package kiwiband.dnb.manager

import kiwiband.dnb.Colors
import kiwiband.dnb.ServerCommunicationManager
import kiwiband.dnb.actors.creatures.Player
import kiwiband.dnb.events.*
import kiwiband.dnb.map.LocalMap
import kiwiband.dnb.math.Vec2

class MultiplayerGameManager(
    private val comm: ServerCommunicationManager,
    var playerId: String,
    var localMap: LocalMap,
    private val eventBus: EventBus
) : GameManager {
    var localPlayer: Player = localMap.findPlayer(playerId) ?: localMap.spawnPlayer(playerId)

    val updateMap: Registration = eventBus.updateMap.addHandler { updateMap(it.newMap) }

    init {
        recolorPlayers()
    }

    private fun updateMap(map: LocalMap) {
        localMap = map
        val player = map.findPlayer(playerId)
        if (player == null) {
            localPlayer.destroy()
            eventBus.run(EventGameOver())
            return
        }
        localPlayer = player
        recolorPlayers()
        map.updateLit()
        eventBus.run(EventTick())
    }

    private fun recolorPlayers() {
        localMap.actors.forEach {
            if (it is Player && it.playerId != playerId) {
                it.appearance.color = Colors.WHITE
                it.appearance.char = (it.playerId[0])
            }
        }
    }

    override fun movePlayer(direction: Vec2) {
        comm.sendEvent(EventMove(direction, playerId))
    }

    override fun useItem(itemNum: Int, playerId: String) {
        comm.sendEvent(EventUseItem(itemNum, playerId))
    }

    override fun finishGame() {
        updateMap.finish()
    }

    override fun startGame() {

    }

    override fun getMap(): LocalMap {
        return localMap
    }

    override fun getPlayer(): Player {
        return localPlayer
    }
}