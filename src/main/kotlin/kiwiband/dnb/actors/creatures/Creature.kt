package kiwiband.dnb.actors.creatures

import kiwiband.dnb.actors.MapActor
import kiwiband.dnb.actors.creatures.status.CreatureStatus
import kiwiband.dnb.map.LocalMap
import kiwiband.dnb.math.Collision
import kiwiband.dnb.math.Vec2

/**
 * @param map currently contained map
 */
abstract class Creature(val map: LocalMap, val status: CreatureStatus) : MapActor() {

    /**
     * Changes position of creature by [direction] and update the map
     */
    open fun move(direction: Vec2) {
        val oldPos = Vec2(pos)
        if (resolveCollision(map.getActors(pos + direction))) {
            pos.add(direction)
            map.actors.updateOne(oldPos)
            map.actors.filterDead(pos)
        }
    }

    fun moveTo(position: Vec2) = move(resolveDirection(position))

    private fun resolveDirection(position: Vec2): Vec2 = (position - pos).normalize()

    private fun resolveCollision(actors: Collection<MapActor>): Boolean {
        var result = true
        for (actor in actors) {
            when (collide(actor)) {
                Collision.Block -> {
                    result = result && blockInteract(actor)
                }
                Collision.Overlap -> overlapInteract(actor)
                else -> {
                }
            }
        }
        return result
    }

    override fun blockInteract(actor: MapActor): Boolean {
        super.blockInteract(actor)
        if (actor is Creature) {
            println("$this hit $actor")
            println("this: $status")
            println("old: ${actor.status}")
            return actor.hit(status.attack).also {
                println("new: ${actor.status}")
            }
        }
        return false
    }

    fun hit(dmg: Int): Boolean {
        status.damage(dmg)
        return status.health == 0
    }

    fun checkDead(): Boolean {
        val isDead = status.health == 0
        if (isDead) {
            onDestroy()
        }
        return isDead
    }
}
