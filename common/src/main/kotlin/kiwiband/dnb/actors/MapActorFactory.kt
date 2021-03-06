package kiwiband.dnb.actors

import kiwiband.dnb.actors.creatures.Mob
import kiwiband.dnb.actors.creatures.Player
import kiwiband.dnb.actors.statics.DropBag
import kiwiband.dnb.actors.statics.WallActor
import kiwiband.dnb.map.LocalMap
import kiwiband.dnb.math.Vec2M
import org.json.JSONObject

object MapActorFactory {
    fun createMapActor(map: LocalMap, actorJSON: JSONObject): MapActor? {
        val x = actorJSON.getInt("x")
        val y = actorJSON.getInt("y")
        return when (actorJSON.getString("t")) {
            WallActor.TYPE_ID -> WallActor(Vec2M(x, y))
            Player.TYPE_ID -> Player.fromJSON(actorJSON, map)
            Mob.TYPE_ID -> Mob.fromJSON(actorJSON, map)
            DropBag.TYPE_ID -> DropBag.fromJSON(actorJSON)
            else -> null
        }
    }
}