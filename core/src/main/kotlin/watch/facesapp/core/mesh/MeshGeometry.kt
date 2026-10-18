package watch.facesapp.core.mesh

data class Point(val x: Float, val y: Float)

data class Edge(val p1: Point, val p2: Point) {
    fun equalsEdge(other: Edge): Boolean {
        return (p1 == other.p1 && p2 == other.p2) || (p1 == other.p2 && p2 == other.p1)
    }
}

data class Triangle(val a: Point, val b: Point, val c: Point) {

    val centroid: Point
        get() = Point((a.x + b.x + c.x) / 3f, (a.y + b.y + c.y) / 3f)

    fun circumcircleContains(p: Point): Boolean {
        val ab = a.x * a.x + a.y * a.y
        val cd = b.x * b.x + b.y * b.y
        val ef = c.x * c.x + c.y * c.y

        val circumX = (ab * (c.y - b.y) + cd * (a.y - c.y) + ef * (b.y - a.y)) /
                (a.x * (c.y - b.y) + b.x * (a.y - c.y) + c.x * (b.y - a.y)) / 2f
        val circumY = (ab * (c.x - b.x) + cd * (a.x - c.x) + ef * (b.x - a.x)) /
                (a.y * (c.x - b.x) + b.y * (a.x - c.x) + c.y * (b.x - a.x)) / 2f

        val dx = a.x - circumX
        val dy = a.y - circumY
        val rSq = dx * dx + dy * dy

        val distSq = (p.x - circumX) * (p.x - circumX) + (p.y - circumY) * (p.y - circumY)
        return distSq <= rSq
    }

    fun containsVertex(p: Point): Boolean {
        return a == p || b == p || c == p
    }

    val edges: List<Edge>
        get() = listOf(Edge(a, b), Edge(b, c), Edge(c, a))
}
