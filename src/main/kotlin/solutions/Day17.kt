package solutions

import utils.Point2D
import java.io.BufferedReader
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.sign
import kotlin.math.sqrt

class Day17(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val startPoint = Point2D(0, 0)

    private val targetArea: Area by lazy {
        val line = inputReader.useLines { it.first() }
        val match = Regex("^target area: x=(-?\\d+)..(-?\\d+), y=(-?\\d+)..(-?\\d+)\$").matchEntire(line)
        match ?: error("Invalid input")
        val (x1, x2, y1, y2) = match.groupValues.drop(1).map { it.toInt() }
        require(x1 <= x2)
        require(y1 <= y2)

        Area(x1..x2, y1..y2)
    }

    override fun solvePart1(): Int {
        val velocityBounds = createVelocityBounds(startPoint, targetArea)

        return velocityBounds.allVelocitiesSequence.maxOf { velocity ->
            val trajectory = createTrajectory(startPoint, velocity)
            if (trajectory.willHitArea(targetArea)) {
                maxY(startPoint, velocity)
            } else {
                Int.MIN_VALUE
            }
        }
    }

    override fun solvePart2(): Int {
        val velocityBounds = createVelocityBounds(startPoint, targetArea)

        return velocityBounds.allVelocitiesSequence.count { velocity ->
            val trajectory = createTrajectory(startPoint, velocity)
            trajectory.willHitArea(targetArea)
        }
    }

    private fun createVelocityBounds(
        start: Point2D,
        targetArea: Area,
    ) : VelocityBounds {
        // max x velocity bound is the one that will hit end of the target area right after start
        val maxXVelocity = targetArea.x.last - start.x
        // min x velocity is the one that, after decreasing to 0, will hit the start of the area
        // got that from sum of arithmetic progression
        val dist = targetArea.x.first - start.x
        val minXVelocity = ceil(0.5*(sqrt((8*dist.absoluteValue+1).toDouble()) - 1)).toInt() * dist.sign
        val minYVelocity = targetArea.y.first - start.y
        val maxYVelocity = (targetArea.y.first - start.y).absoluteValue - 1

        return VelocityBounds(minXVelocity..maxXVelocity, minYVelocity..maxYVelocity)
    }

    private fun createTrajectory(
        start: Point2D,
        velocity: Point2D,
    ): Sequence<ProbeState> = generateSequence(ProbeState(start, velocity)) { previous ->
        ProbeState(
            position = previous.position + previous.velocity,
            velocity = Point2D(
                x = previous.velocity.x.let { prev ->
                    when {
                        prev < 0 -> prev + 1
                        prev > 0 -> prev - 1
                        else -> 0
                    }
                },
                y = previous.velocity.y - 1,
            ),
        )
    }

    /**
     * Returns true if this Trajectory will ever hit the [area]
     */
    private fun Sequence<ProbeState>.willHitArea(area: Area): Boolean {
        return map {
            it to probeToAreaState(it, area)
        }.takeWhile {
            it.second.isAfter.not()
        }.firstOrNull {
            it.second.isWithin
        } != null
    }

    private fun maxY(
        start: Point2D,
        velocity: Point2D,
    ): Int {
        if (velocity.y <= 0) return start.y
        return start.y + velocity.y * (velocity.y + 1) / 2
    }

    private fun probeToAreaState(
        probe: ProbeState,
        area: Area,
    ) : ProbeToAreaState {
        val xDiff = area.x.first - probe.position.x
        val xState = when {
            probe.position.x in area.x -> ProbeToAreaCoordState.Within
            xDiff.sign == probe.velocity.x.sign -> ProbeToAreaCoordState.Before
            else -> ProbeToAreaCoordState.After
        }

        val yDiff = area.y.first - probe.position.y
        val yState = when {
            probe.position.y in area.y -> ProbeToAreaCoordState.Within
            probe.velocity.y >= 0 -> ProbeToAreaCoordState.Before
            yDiff.sign == probe.velocity.y.sign -> ProbeToAreaCoordState.Before
            else -> ProbeToAreaCoordState.After
        }
        return ProbeToAreaState(
            x = xState,
            y = yState,
        )
    }

    private class VelocityBounds(
        val x: IntRange,
        val y: IntRange,
    ) {
        val allVelocitiesSequence: Sequence<Point2D> = x.asSequence().flatMap { xVelocity ->
            y.asSequence().map { yVelocity -> Point2D(xVelocity, yVelocity) }
        }
    }

    private data class Area(
        val x: IntRange,
        val y: IntRange,
    )

    private data class ProbeState(
        val position: Point2D,
        val velocity: Point2D,
    )

    private data class ProbeToAreaState(
        val x: ProbeToAreaCoordState,
        val y: ProbeToAreaCoordState,
    ) {
        val isWithin = x == ProbeToAreaCoordState.Within && y == ProbeToAreaCoordState.Within
        val isAfter = x == ProbeToAreaCoordState.After || y == ProbeToAreaCoordState.After
    }

    private enum class ProbeToAreaCoordState {
        Before, Within, After
    }
}
