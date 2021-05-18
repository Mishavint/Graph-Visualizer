package visualizer.controller

import javafx.animation.AnimationTimer
import visualizer.model.ForceAtlas2
import visualizer.view.GraphView
import tornadofx.FX.Companion.log

class FA2Controller : AnimationTimer() {
    private lateinit var fa2: ForceAtlas2
    private var speed = 1.0
    private var speedEfficiency = 1.0
    private var jitterTolerance = 1.0
    private var scalingRatio = 100.0
    private var strongGravityMode = false
    private var gravity = 1.0
    private var barnesHutOptimize = false
    private var barnesHutTheta = 1.0
    private var status = false

    override fun handle(now: Long) {
        fa2.runAlgo()
    }

    fun runFA(): Boolean {
        if (!status) {
            this.start()
            status = !status
            log.info("FA2 was launched")
        } else {
            this.stop()
            status = !status
            log.info("FA2 was stopped")
        }

        return status
    }

    fun speed(speed: Double) {
        this.speed = speed
    }

    fun speedEfficiency(speedEfficiency: Double) {
        this.speedEfficiency = speedEfficiency
    }

    fun jitterTolerance(jitterTolerance: Double) {
        this.jitterTolerance = jitterTolerance
    }

    fun scalingRatio(scalingRatio: Double) {
        this.scalingRatio = scalingRatio
    }

    fun strongGravityMode(strongGravityMode: Boolean) {
        this.strongGravityMode = strongGravityMode
    }

    fun gravity(gravity: Double) {
        this.gravity = gravity
    }

    fun barnesHutOptimize(barnesHutOptimize: Boolean) {
        this.barnesHutOptimize = barnesHutOptimize
    }

    fun barnesHutTheta(barnesHutTheta: Double) {
        this.barnesHutTheta = barnesHutTheta
    }

    fun prepareFA2(graph: GraphView) {
        fa2 = ForceAtlas2(graph)
    }

    fun applySettings() {
        if (!this::fa2.isInitialized) return
        fa2.speed = this.speed
        fa2.speedEfficiency = this.speedEfficiency
        fa2.jitterTolerance = this.jitterTolerance
        fa2.scalingRatio = this.scalingRatio
        fa2.strongGravityMode = this.strongGravityMode
        fa2.gravity = this.gravity
        fa2.barnesHutOptimize = this.barnesHutOptimize
        fa2.barnesHutTheta = this.barnesHutTheta
        log.info("Settings changes were applied")
    }
}