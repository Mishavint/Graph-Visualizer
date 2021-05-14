package visualizer.controller

import javafx.concurrent.ScheduledService
import javafx.concurrent.Task
import javafx.util.Duration

import visualizer.model.ForceAtlas2
import visualizer.view.GraphView

class FA2Controller(var graph: GraphView, private val millis: Double = 80.0) {
    private val fa2 = ForceAtlas2(graph)
    private var tm = TaskManager(millis)
    private var status = false

    fun runFA() {
        if (!status) {
            tm = TaskManager(millis)
            tm.start()
            status = !status
        }
        else {
            tm.cancel()
            status = !status
        }
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

    private inner class TaskManager(millis: Double): ScheduledService<Unit>() {
        init {
            period = Duration(millis)
        }

        override fun createTask(): Task<Unit> = IterationTask()

        private inner class IterationTask : Task<Unit>() {
            override fun call() {
                fa2.runAlgo()
            }
        }
    }
}