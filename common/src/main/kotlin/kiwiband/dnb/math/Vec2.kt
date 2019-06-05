package kiwiband.dnb.math

/**
 * Immutable two-dimensional point.
 * @param x X coordinate
 * @param y Y coordinate
 */
@Suppress("unused")
open class Vec2(var x: Int, var y: Int) {
    constructor(v: Vec2) : this(v.x, v.y)
    constructor() : this(0, 0)

    fun isZero() : Boolean = x == 0 && y == 0

    operator fun unaryMinus() = Vec2(-x, -y)

    operator fun plus(v: Vec2) = Vec2(x + v.x, y + v.y)

    operator fun minus(v: Vec2) = Vec2(x - v.x, y - v.y)

    fun mixMax(v: Vec2): Vec2 = Vec2(Math.max(x, v.x), Math.max(y, v.y))

    fun mixMin(v: Vec2): Vec2 = Vec2(Math.min(x, v.x), Math.min(y, v.y))

    override fun equals(other: Any?): Boolean {
        if (other is Vec2) {
            return x == other.x && y == other.y
        }
        return super.equals(other)
    }


    fun distance(v: Vec2M): Int = distance(v.x, v.y)

    fun distance(vx: Int, vy: Int): Int = Math.abs(vx - x) + Math.abs(vy - y)

    fun normalize(): Vec2M = Vec2M(Integer.signum(x), Integer.signum(y))

    /**
     * Fits a point in borders.
     * @borders borders to fit in
     * @return fitted point
     */
    fun fitIn(borders: Borders) = Vec2(
        MyMath.clamp(x, borders.a.x, borders.b.x - 1),
        MyMath.clamp(y, borders.a.y, borders.b.y - 1)
    )

    /**
     * Fits a point in borders with included bottom right corner.
     * @borders borders to fit in
     * @return fitted point
     */
    fun fitInIncluded(borders: Borders) = Vec2(
        MyMath.clamp(x, borders.a.x, borders.b.x),
        MyMath.clamp(y, borders.a.y, borders.b.y)
    )


    infix fun to(that: Vec2): Borders = Borders(this, that)

    override fun hashCode(): Int = 0xffff * x + y
}