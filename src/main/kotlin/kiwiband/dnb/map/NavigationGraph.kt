package kiwiband.dnb.map

import kiwiband.dnb.actors.MapActor
import kiwiband.dnb.actors.creatures.Creature
import kiwiband.dnb.math.Collision
import kiwiband.dnb.math.Vec2M
import java.util.*

class NavigationGraph(val map: MapGrid) {
    private val marks: IntArray = IntArray(map.height * map.width) { -1 }
    private val dists: IntArray = IntArray(map.height * map.width) { -1 }
    private val deque = ArrayDeque<MapCell>()


    fun pathToNearest(start: Vec2M, maxDistance: Int, f: (Collection<MapActor>) -> Boolean): MutableList<Vec2M>? {
        val nearestCell = bfs(start, maxDistance) { cell -> f(cell.getActors()) }
        return nearestCell?.let { findPath(it.getId()) }
    }

    fun nextToNearest(start: Vec2M, maxDistance: Int, f: (Collection<MapActor>) -> Boolean): Vec2M? {
        return bfs(start, maxDistance + 1) { cell ->
            cell.neighbours().any { neighbour ->
                val dist = dists[neighbour.getId()]
                0 < dist && dist < dists[cell.getId()] && f(neighbour.getActors())
            }
        }?.toVec2M()
    }

    fun nearest(start: Vec2M, maxDistance: Int, f: (Collection<MapActor>) -> Boolean): Vec2M? {
        return bfs(start, maxDistance) { cell -> f(cell.getActors()) }?.toVec2M()
    }

    private fun bfs(start: Vec2M, maxDistance: Int, f: (MapCell) -> Boolean): MapCell? {
        marks.fill(-1)
        dists.fill(-1)
        deque.clear()

        val startCell = MapCell(start.x, start.y)
        marks[startCell.getId()] = startCell.getId()
        dists[startCell.getId()] = 0
        deque.addLast(startCell)
        while (deque.isNotEmpty()) {
            val cell = deque.pollFirst()
            val cellDist = start.distance(cell.x, cell.y)
            if (cellDist > maxDistance) return null
            if (f(cell)) {
                return cell
            }
            val id = cell.getId()
            val dist = dists[id]
            cell.neighbours().shuffled().forEach {
                val itId = it.getId()
                if (marks[itId] < 0) {
                    deque.addLast(it)
                    marks[itId] = id
                    dists[itId] = dist + 1
                }
            }
        }
        return null
    }

    private fun findPath(startId: Int): MutableList<Vec2M> {
        val path = mutableListOf<Vec2M>()
        var id = startId
        while (marks[id] != id) {
            path.add(toVec2M(id))
            id = marks[id]
        }
        return path
    }

    private fun toVec2M(id: Int): Vec2M {
        val y: Int = id / map.width
        return Vec2M(id - y * map.width, y)
    }

    inner class MapCell(val x: Int, val y: Int) {
        fun getActors() = map[x, y]

        fun getId() = y * map.width + x

        fun neighbours(): Iterable<MapCell> {
            MapCellList.clear()
            if (checkCell(x, y + 1)) MapCellList.add(MapCell(x, y + 1))
            if (checkCell(x, y - 1)) MapCellList.add(MapCell(x, y - 1))
            if (checkCell(x + 1, y)) MapCellList.add(MapCell(x + 1, y))
            if (checkCell(x - 1, y)) MapCellList.add(MapCell(x - 1, y))
            return MapCellList
        }

        fun toVec2M(): Vec2M {
            return Vec2M(x, y)
        }

        private fun checkCell(x: Int, y: Int): Boolean =
            map[x, y].all { it.collision != Collision.Block || it is Creature }
    }

    companion object {
        val MapCellList: MutableList<MapCell> = mutableListOf()
    }
}
