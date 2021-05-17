package visualizer.model

import visualizer.view.GraphView

class ForceAtlas2(var graph: GraphView) {
    private var nodes = mutableListOf<Node>()
    private var edges = mutableListOf<Edge>()

    var speed = 1.0
    var speedEfficiency = 1.0

    var jitterTolerance = 1.0

    var scalingRatio = 100.0
    var strongGravityMode = false
    var gravity = 1.0

    private var outboundAttractionDistribution = 1.0
    private var outboundAttCompensation = false
    private var edgeWeightInfluence = 1.0

    var barnesHutOptimize = false
    var barnesHutTheta = 1.0

    init {
        initAlgo()
    }

    inner class Node {
        var id = "0"
        var mass = 0.0
        var oldDX = 0.0
        var oldDY = 0.0
        var dx = 0.0
        var dy = 0.0
        var x = 0.0
        var y = 0.0
    }

    inner class Edge {
        lateinit var node1: Node
        lateinit var node2: Node
        private var weight = 0.0
    }

    inner class Region(nodes: MutableList<Node>) {
        var mass = 0.0
        var massCenterX = 0.0
        var massCenterY = 0.0
        var size = 0.0

        var nodes = nodes
        var subregions = mutableListOf<Region>()
    }

    fun runAlgo() {
        resetNodesVelocity()
        applyBarnesOrRepulsion()
        applyGravity()
        applyAttraction()
        applyAdjustingSpeedAndApplyForces()
        applyReplacement()
    }

    private fun resetNodesVelocity() {
        nodes.onEach { it.oldDX = it.dx; it.oldDY = it.dy; it.dx = 0.0; it.dy = 0.0 }
    }

    private fun applyBarnesOrRepulsion() {
        if (barnesHutOptimize) {
            val rootRegion = Region(nodes)
            updateMassAndGeometry(rootRegion)
            buildSubRegion(rootRegion)
            applyForceOnNodes(rootRegion, barnesHutTheta, scalingRatio)
        } else {
            for (n1 in nodes) {
                for (n2 in nodes) {
                    if (n1.id != n2.id) linRepulsion(n1, n2, scalingRatio)
                }
            }
        }
    }

    private fun applyGravity() {
        if (!strongGravityMode) for (node in nodes) linGravity(node, gravity)
        else for (node in nodes) strongGravity(node, gravity)
    }

    private fun applyAttraction() {
        for (edge in edges) linAttraction(
            edge.node1,
            edge.node2,
            outboundAttractionDistribution,
            outboundAttCompensation,
            edgeWeightInfluence
        )
    }

    private fun applyReplacement() {
        graph.vertices().values.onEachIndexed { count, it -> it.position = nodes[count].x to nodes[count].y }
    }

    private fun applyAdjustingSpeedAndApplyForces() {
        val values = adjustSpeedAndApplyForces(speed, speedEfficiency, jitterTolerance)
        speed = values.first
        speedEfficiency = values.second
    }

    private fun initAlgo() {
        for (vertex in graph.vertices()) {
            val n = Node()
            n.oldDX = 0.0
            n.oldDY = 0.0
            n.dx = 0.0
            n.dy = 0.0
            n.mass = 1.0

            nodes.add(n)
        }

        graph.vertices().values.onEachIndexed { count, it ->
            nodes[count].id = it.vertex.element; nodes[count].x = it.position.first; nodes[count].y =
            it.position.second
        }

        for (edge in graph.edges()) {
            val e = Edge()
            e.node1 = nodes[findNodeWithId(edge.key.vertices.first.element)]
            e.node2 = nodes[findNodeWithId(edge.key.vertices.second.element)]
            edges.add(e)
        }
    }

    private fun findNodeWithId(neededId: String): Int {
        for (i in 0 until nodes.size) {
            if (nodes[i].id == neededId) return i
        }

        return -1
    }

    private fun linGravity(n: Node, g: Double) {
        val xDist = n.x
        val yDist = n.y

        val distance = Math.sqrt(xDist * xDist + yDist * yDist)

        if (distance > 0) {
            val factor = n.mass * g / distance
            n.dx -= xDist * factor
            n.dy -= yDist * factor
        }
    }

    private fun strongGravity(n: Node, g: Double, coefficient: Double = 0.0) {
        val xDist = n.x
        val yDist = n.y

        if (xDist != 0.0 && yDist != 0.0) {
            val factor = coefficient * n.mass * g
            n.dx -= xDist * factor
            n.dy -= yDist * factor
        }
    }

    private fun linRepulsion(n1: Node, n2: Node, coefficient: Double = 0.0) {
        val xDist = n1.x - n2.x
        val yDist = n1.y - n2.y

        val distance = xDist * xDist + yDist * yDist

        if (distance > 0) {
            val factor = coefficient * n1.mass * n2.mass / distance
            n1.dx += xDist * factor
            n1.dy += yDist * factor
            n2.dx -= xDist * factor
            n2.dy -= yDist * factor
        }
    }

    private fun linRepulsion_region(n: Node, r: Region, coefficient: Double = 0.0) {
        val xDist = n.x - r.massCenterX
        val yDist = n.y - r.massCenterY
        val distance = xDist * xDist + yDist * yDist

        if (distance > 0) {
            val factor = coefficient * n.mass * r.mass / distance
            n.dx += xDist * factor
            n.dy += yDist * factor
        }
    }

    private fun linAttraction(
        n1: Node,
        n2: Node,
        e: Double,
        distributedAttraction: Boolean,
        coefficient: Double = 0.0
    ) {
        val xDist = n1.x - n2.x
        val yDist = n1.y - n2.y

        val factor = if (!distributedAttraction) -coefficient * e
        else -coefficient * e / n1.mass

        n1.dx += xDist * factor
        n1.dy += yDist * factor
        n2.dx -= xDist * factor
        n2.dy -= yDist * factor
    }

    private fun adjustSpeedAndApplyForces(
        speed: Double,
        speedEfficiency: Double,
        jitterTolerance: Double
    ): Pair<Double, Double> {
        var speed = speed
        var speedEfficiency = speedEfficiency

        var totalSwinging = 0.0
        var totalEffectiveTraction = 0.0

        for (n in nodes) {
            val swinging = Math.sqrt((n.oldDX - n.dx) * (n.oldDX - n.dx) + (n.oldDY - n.dy) * (n.oldDY - n.dy))
            totalSwinging += n.mass * swinging
            totalEffectiveTraction += .5 * n.mass * Math.sqrt(
                (n.oldDX + n.dx) * (n.oldDX + n.dx) + (n.oldDY + n.dy) * (n.oldDY + n.dy)
            )
        }

        val estimatedOptimalJitterTolerance = .05 * Math.sqrt(nodes.size.toDouble())
        val minJT = Math.sqrt(estimatedOptimalJitterTolerance)
        val maxJT = 10.0
        var jt = jitterTolerance * maxOf(
            minJT,
            minOf(
                maxJT, estimatedOptimalJitterTolerance * totalEffectiveTraction / (
                        nodes.size * nodes.size)
            )
        )

        val minSpeedEfficiency = 0.05

        if (totalEffectiveTraction > 2.0 && totalSwinging / totalEffectiveTraction > 2.0) {
            if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= .5
            jt = maxOf(jt, jitterTolerance)
        }

        val targetSpeed = if (totalSwinging == 0.0) Double.POSITIVE_INFINITY
        else jt * speedEfficiency * totalEffectiveTraction / totalSwinging

        if (totalSwinging > jt * totalEffectiveTraction) if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= .7
        else if (speed < 1000) speedEfficiency *= 1.3

        val maxRise = .5
        speed += minOf(targetSpeed - speed, maxRise * speed)

        for (n in nodes) {
            val swinging =
                n.mass * Math.sqrt((n.oldDX - n.dx) * (n.oldDX - n.dx) + (n.oldDY - n.dy) * (n.oldDY - n.dy))
            val factor = speed / (1.0 + Math.sqrt(speed * swinging))
            n.x = n.x + (n.dx * factor)
            n.y = n.y + (n.dy * factor)
        }

        return speed to speedEfficiency
    }

    private fun updateMassAndGeometry(r: Region) {
        if (r.nodes.size > 1) {
            r.mass = 0.0
            var massSumX = 0.0
            var massSumY = 0.0

            for (n in r.nodes) {
                r.mass += n.mass
                massSumX += n.x * n.mass
                massSumY += n.y * n.mass
            }

            r.massCenterX = massSumX / r.mass
            r.massCenterY = massSumY / r.mass

            r.size = 0.0
            for (n in r.nodes) {
                val distance =
                    Math.sqrt((n.x - r.massCenterX) * (n.x - r.massCenterX) + (n.y - r.massCenterY) * (n.y - r.massCenterY))
                r.size = maxOf(r.size, 2 * distance)
            }
        }
    }

    private fun buildSubRegion(r: Region) {
        if (r.nodes.size > 1) {
            val topleftNodes: MutableList<Node> = mutableListOf()
            val bottomleftNodes: MutableList<Node> = mutableListOf()
            val toprightNodes: MutableList<Node> = mutableListOf()
            val bottomrightNodes: MutableList<Node> = mutableListOf()

            for (n in r.nodes) {
                if (n.x < r.massCenterX) {
                    if (n.y < r.massCenterY) bottomleftNodes.add(n)
                    else topleftNodes.add(n)
                } else {
                    if (n.y < r.massCenterY) bottomrightNodes.add(n)
                    else toprightNodes.add(n)
                }
            }

            if (topleftNodes.size > 0) {
                if (topleftNodes.size < r.nodes.size) {
                    val subregion = Region(topleftNodes)
                    updateMassAndGeometry(subregion)
                    r.subregions.add(subregion)
                } else {
                    for (n in topleftNodes) {
                        val tmp = mutableListOf(n)
                        val subregion = Region(tmp)
                        updateMassAndGeometry(subregion)
                        r.subregions.add(subregion)
                    }
                }
            }

            if (bottomleftNodes.size > 0) {
                if (bottomleftNodes.size < r.nodes.size) {
                    val subregion = Region(bottomleftNodes)
                    updateMassAndGeometry(subregion)
                    r.subregions.add(subregion)
                } else {
                    for (n in bottomleftNodes) {
                        val tmp = mutableListOf(n)
                        val subregion = Region(tmp)
                        updateMassAndGeometry(subregion)
                        r.subregions.add(subregion)
                    }
                }
            }

            if (toprightNodes.size > 0) {
                if (toprightNodes.size < r.nodes.size) {
                    val subregion = Region(toprightNodes)
                    updateMassAndGeometry(subregion)
                    r.subregions.add(subregion)
                } else {
                    for (n in toprightNodes) {
                        val tmp = mutableListOf(n)
                        val subregion = Region(tmp)
                        updateMassAndGeometry(subregion)
                        r.subregions.add(subregion)
                    }
                }
            }

            if (bottomrightNodes.size > 0) {
                if (bottomrightNodes.size < r.nodes.size) {
                    val subregion = Region(bottomrightNodes)
                    updateMassAndGeometry(subregion)
                    r.subregions.add(subregion)
                } else {
                    for (n in bottomrightNodes) {
                        val tmp = mutableListOf(n)
                        val subregion = Region(tmp)
                        updateMassAndGeometry(subregion)
                        r.subregions.add(subregion)
                    }
                }
            }

            for (subregion in r.subregions) buildSubRegion(subregion)
        }
    }

    private fun applyForce(r: Region, n: Node, theta: Double, coefficient: Double = 0.0) {
        if (r.nodes.size < 2) linRepulsion(n, r.nodes[0], coefficient)
        else {
            val distance =
                Math.sqrt((n.x - r.massCenterX) * (n.x - r.massCenterX) + (n.y - r.massCenterY) * (n.y - r.massCenterY))
            if (distance * theta > r.size) linRepulsion_region(n, r, coefficient)
            else for (subregion in r.subregions) applyForce(subregion, n, theta, coefficient)
        }
    }

    private fun applyForceOnNodes(r: Region, theta: Double, coefficient: Double = 0.0) {
        for (n in r.nodes) applyForce(r, n, theta, coefficient)
    }
}