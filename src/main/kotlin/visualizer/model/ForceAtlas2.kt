package visualizer.model

import visualizer.view.GraphView

class ForceAtlas2(var graph: GraphView) {
    var nodes = mutableListOf<Node>() // !!! IM SORRY FOR THE SH*T YOU ARE ABOUT TO SEE HERE
    var edges = mutableListOf<Edge>() // !!!

    var speed = 1.0
    var speedEfficiency = 1.0

    var jitterTolerance = 1.0

    var scalingRatio = 100.0 // Important thing
    var strongGravityMode = false
    var gravity = 1.0

    var outboundAttractionDistribution = 1.0 // Do not change (works)
    var outboundAttCompensation = false // Do not change (works)
    var edgeWeightInfluence = 1.0 // Do not change (mixed)

    var barnesHutOptimize =  true
    var barnesHutTheta = 1.0

    inner class Node {
        var mass = 0.0
        var old_dx = 0.0
        var old_dy = 0.0
        var dx = 0.0
        var dy = 0.0
        var x = 0.0
        var y = 0.0
    }

    inner class Edge {
        lateinit var node1: Node
        lateinit var node2: Node
        var weight = 0.0
    }

    inner class Region(nodes: MutableList<Node>) {
        var mass = 0.0
        var massCenterX = 0.0
        var massCenterY = 0.0
        var size = 0.0

        var nodes = nodes
        var subregions: MutableList<Region> = mutableListOf()

        /*init {
            updateMassAndGeometry(this)
        }*/
    }

    init {
        initAlgo()
    }

    private fun initAlgo() {
        for (i in 0..graph.vertices().size) {
            var n = Node()
            n.old_dx = 0.0
            n.old_dy = 0.0
            n.dx = 0.0
            n.dy = 0.0
            //n.mass = Math.random() // works
            n.mass = 1.0

            nodes.add(n)
        }
        var count = 0
        graph.vertices().values.onEach { nodes[count].x = it.centerX; nodes[count].y = it.centerY; count++ }

        //--edges--//OLD_hardcode
        /*var edge = Edge()
        edge.node1 = nodes[0]
        edge.node2 = nodes[1]
        edges.add(edge)
        for (i in 2..7) {
            edge = Edge()
            edge.node1 = nodes[0]
            edge.node2 = nodes[i]
            edges.add(edge)
        }

        for (i in 8..13) {
            edge = Edge()
            edge.node1 = nodes[7]
            edge.node2 = nodes[i]
            edges.add(edge)
        }*/

        //==NEW==//
        var stage1 = mutableListOf<String>()
        var stage2 = mutableListOf<String>()
        var stage3 = mutableListOf<Int>()
        for (i in graph.vertices().keys) {
            stage1.add(i.element)
        }

        for (i in graph.edges().keys) {
            stage2.add(i.vertices.first.element)
            stage2.add(i.vertices.second.element)
        }

        stage2.forEach { stage3.add(stage1.indexOf(it)) }

        var stage4 = stage3.windowed(2, 2, true) {
            if (it.size % 2 != 0) listOf(it.first(), null) else it.toList()
        }

        for (pair in stage4) {
            var edge = Edge()
            edge.node1 = nodes[pair.first()!!] //; println("=============${pair.first()}")
            edge.node2 = nodes[pair.last()!!] //; println("==============${pair.last()}")
            edges.add(edge)
        }
    }

    public fun runAlgo() {
        for (n in nodes) {
            n.old_dx = n.dx
            n.old_dy = n.dy
            n.dx = 0.0
            n.dy = 0.0
        }

        //Applying BarnesHut optimization OR Repulsion
        if (barnesHutOptimize) {
            var rootRegion = Region(nodes)
            updateMassAndGeometry(rootRegion)
            buildSubRegion(rootRegion)
            applyForceOnNodes(rootRegion, barnesHutTheta, scalingRatio)
        }
        else {
            var i = 0; for (n1 in nodes) { var j = i; for (n2 in nodes) { if (j == 0) break; linRepulsion(n1, n2, scalingRatio); j -= 1 }; i += 1 } }

        //Applying Gravity
        if (!strongGravityMode) for (node in nodes) linGravity(node, gravity)
        else for (node in nodes) strongGravity(node, gravity)

        //Applying Attraction
        for (edge in edges) linAttraction(edge.node1, edge.node2, outboundAttractionDistribution, outboundAttCompensation, edgeWeightInfluence)

        var values = adjustSpeedAndApplyForces(speed, speedEfficiency, jitterTolerance)
        speed = values.first
        speedEfficiency = values.second

        //Resetting positions after applying forces // SHIT STUFF
        var count = 0
        graph.vertices().values.onEach { it.position = nodes[count].x to nodes[count].y; count++ }
    }

    private fun linGravity(n: Node, g: Double) {
        var xDist = n.x
        var yDist = n.y

        var distance = Math.sqrt(xDist * xDist + yDist * yDist)

        if (distance > 0) {
            var factor = n.mass * g / distance
            n.dx -= xDist * factor
            n.dy -= yDist * factor
        }
    }

    private fun strongGravity(n: Node, g: Double, coefficient: Double = 0.0) {
        var xDist = n.x
        var yDist = n.y

        if (xDist != 0.0 && yDist != 0.0) {
            var factor = coefficient * n.mass * g
            n.dx -= xDist * factor
            n.dy -= yDist * factor
        }
    }

    private fun linRepulsion(n1: Node, n2: Node, coefficient: Double = 0.0) {
        var xDist = n1.x - n2.x
        var yDist = n1.y - n2.y

        var distance = xDist * xDist + yDist * yDist

        if (distance > 0) {
            var factor = coefficient * n1.mass * n2.mass / distance
            n1.dx += xDist * factor
            n1.dy += yDist * factor
            n2.dx -= xDist * factor
            n2.dy -= yDist * factor
        }
    }

    private fun linRepulsion_region(n: Node, r: Region, coefficient: Double = 0.0) {
        var xDist = n.x - r.massCenterX
        var yDist = n.y - r.massCenterY
        var distance = xDist * xDist + yDist * yDist

        if (distance > 0) {
            var factor = coefficient * n.mass * r.mass / distance
            n.dx += xDist * factor
            n.dy += yDist * factor
        }
    }

    private fun linAttraction(n1: Node, n2: Node, e: Double, distributedAttraction: Boolean, coefficient: Double = 0.0) {
        var xDist = n1.x - n2.x
        var yDist = n1.y - n2.y

        var factor = if (!distributedAttraction) -coefficient * e
        else -coefficient * e / n1.mass

        n1.dx += xDist * factor
        n1.dy += yDist * factor
        n2.dx -= xDist * factor
        n2.dy -= yDist * factor
    }

    private fun adjustSpeedAndApplyForces(speed: Double, speedEfficiency: Double, jitterTolerance: Double): Pair<Double, Double> {
        var speed = speed
        var speedEfficiency = speedEfficiency

        var totalSwinging = 0.0
        var totalEffectiveTraction = 0.0

        for (n in nodes) {
            var  swinging = Math.sqrt((n.old_dx - n.dx) * (n.old_dx - n.dx) + (n.old_dy - n.dy) * (n.old_dy - n.dy))
            totalSwinging += n.mass * swinging
            totalEffectiveTraction += .5 * n.mass * Math.sqrt(
                (n.old_dx + n.dx) * (n.old_dx + n.dx) + (n.old_dy + n.dy) * (n.old_dy + n.dy))
        }

        var estimatedOptimalJitterTolerance = .05 * Math.sqrt(nodes.size.toDouble())
        var minJT = Math.sqrt(estimatedOptimalJitterTolerance)
        var maxJT = 10.0
        var jt = jitterTolerance * maxOf(minJT,
            minOf(maxJT, estimatedOptimalJitterTolerance * totalEffectiveTraction / (
                    nodes.size * nodes.size)))

        var minSpeedEfficiency = 0.05

        if (totalEffectiveTraction > 2.0 && totalSwinging / totalEffectiveTraction > 2.0) {
            if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= .5
            jt = maxOf(jt, jitterTolerance)
        }

        var targetSpeed = if (totalSwinging == 0.0) Double.POSITIVE_INFINITY
        else jt * speedEfficiency * totalEffectiveTraction / totalSwinging

        if (totalSwinging > jt * totalEffectiveTraction) if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= .7
        else if (speed < 1000) speedEfficiency *= 1.3

        var maxRise = .5
        speed  += minOf(targetSpeed - speed, maxRise * speed)

        for (n in nodes) {
            var swinging = n.mass * Math.sqrt((n.old_dx - n.dx) * (n.old_dx - n.dx) + (n.old_dy - n.dy) * (n.old_dy - n.dy))
            var factor = speed / (1.0 + Math.sqrt(speed * swinging))
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

            for(n in r.nodes) {
                r.mass += n.mass
                massSumX += n.x * n.mass
                massSumY += n.y * n.mass
            }

            r.massCenterX = massSumX / r.mass
            r.massCenterY = massSumY / r.mass

            r.size = 0.0
            for (n in r.nodes) {
                var distance = Math.sqrt((n.x - r.massCenterX) * (n.x - r.massCenterX) + (n.y - r.massCenterY) * (n.y - r.massCenterY))
                r.size = maxOf(r.size, 2 * distance)
            }
        }
    }

    private fun buildSubRegion(r: Region) {
        if (r.nodes.size > 1) {
            var topleftNodes: MutableList<Node> = mutableListOf()
            var bottomleftNodes: MutableList<Node> = mutableListOf()
            var toprightNodes: MutableList<Node> = mutableListOf()
            var bottomrightNodes: MutableList<Node> = mutableListOf()

            for (n in r.nodes) {
                if (n.x < r.massCenterX) {
                    if (n.y < r.massCenterY) bottomleftNodes.add(n)
                    else topleftNodes.add(n)
                }

                else {
                    if (n.y < r.massCenterY) bottomrightNodes.add(n)
                    else toprightNodes.add(n)
                }
            }

            if (topleftNodes.size > 0) {
                if (topleftNodes.size < r.nodes.size) {
                    var subregion = Region(topleftNodes)
                    updateMassAndGeometry(subregion) // +++
                    r.subregions.add(subregion)
                }
                else {
                    for (n in topleftNodes) {
                        var tmp = mutableListOf(n)
                        var subregion = Region(tmp) //!!!!!!
                        updateMassAndGeometry(subregion) // +++
                        r.subregions.add(subregion)
                    }
                }
            }

            if (bottomleftNodes.size > 0) {
                if (bottomleftNodes.size < r.nodes.size) {
                    var subregion = Region(bottomleftNodes)
                    updateMassAndGeometry(subregion) // +++
                    r.subregions.add(subregion)
                }
                else {
                    for (n in bottomleftNodes) {
                        var tmp = mutableListOf(n)
                        var subregion = Region(tmp) //!!!!!!
                        updateMassAndGeometry(subregion) // +++
                        r.subregions.add(subregion)
                    }
                }
            }

            if (toprightNodes.size > 0) {
                if (toprightNodes.size < r.nodes.size) {
                    var subregion = Region(toprightNodes)
                    updateMassAndGeometry(subregion) // +++
                    r.subregions.add(subregion)
                }
                else {
                    for (n in toprightNodes) {
                        var tmp = mutableListOf(n)
                        var subregion = Region(tmp) //!!!!!!
                        updateMassAndGeometry(subregion) // +++
                        r.subregions.add(subregion)
                    }
                }
            }

            if (bottomrightNodes.size > 0) {
                if (bottomrightNodes.size < r.nodes.size) {
                    var subregion = Region(bottomrightNodes)
                    updateMassAndGeometry(subregion) // +++
                    r.subregions.add(subregion)
                }
                else {
                    for (n in bottomrightNodes) {
                        var tmp = mutableListOf(n)
                        var subregion = Region(tmp) //!!!!!!
                        updateMassAndGeometry(subregion) // +++
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
            var distance = Math.sqrt((n.x - r.massCenterX) * (n.x - r.massCenterX) + (n.y - r.massCenterY) * (n.y - r.massCenterY))
            if (distance * theta > r.size) linRepulsion_region(n, r, coefficient)
            else for (subregion in r.subregions) applyForce(subregion, n, theta, coefficient)
        }
    }

    private fun applyForceOnNodes(r: Region, theta: Double, coefficient: Double = 0.0) {
        for (n in r.nodes) applyForce(r, n, theta, coefficient)
    }
}