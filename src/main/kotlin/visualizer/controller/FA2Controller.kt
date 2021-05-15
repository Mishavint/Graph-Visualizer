package visualizer.controller

import javafx.animation.AnimationTimer

import visualizer.model.ForceAtlas2
import visualizer.view.GraphView

class FA2Controller : AnimationTimer() {
    private lateinit var fa2:ForceAtlas2
    private var status = false

    override fun handle(now: Long) {
        fa2.runAlgo()
    }

    fun runFA(graph: GraphView): Boolean {
        if (!status) {
            fa2 = ForceAtlas2(graph)
            this.start()
            status = !status
        } else {
            this.stop()
            status = !status
        }

        return status
    }

    fun speed(speed: Double) {
        fa2.speed = speed
    }

    fun speedEfficiency(speedEfficiency: Double) {
        fa2.speedEfficiency = speedEfficiency
    }

    fun jitterTolerance(jitterTolerance: Double) {
        fa2.jitterTolerance = jitterTolerance
    }

    fun scalingRatio(scalingRatio: Double) {
        fa2.scalingRatio = scalingRatio
    }

    fun strongGravityMode(strongGravityMode: Boolean) {
        fa2.strongGravityMode = strongGravityMode
    }

    fun gravity(gravity: Double) {
        fa2.gravity = gravity
    }

    fun barnesHutOptimize(barnesHutOptimize: Boolean) {
        fa2.barnesHutOptimize = barnesHutOptimize
    }

    fun barnesHutTheta(barnesHutTheta: Double) {
        fa2.barnesHutTheta = barnesHutTheta
    }
}