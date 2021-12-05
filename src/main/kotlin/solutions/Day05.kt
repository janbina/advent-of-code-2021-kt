package solutions

import utils.Point2D
import java.io.BufferedReader
import kotlin.math.absoluteValue

class Day05(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: List<VentureLine> by lazy {
        inputReader.transformLines { line ->
            val (x1, y1, x2, y2) = line.split(",", " -> ").map { it.toInt() }
            VentureLine(
                start = Point2D(x1, y1),
                end = Point2D(x2, y2),
            )
        }
    }

    override fun solvePart1(): Int {
        return input.asSequence()
            .filter {
                it.start.x == it.end.x || it.start.y == it.end.y
            }
            .flatMap { it.pointSequence() }
            .groupingBy { it }
            .eachCount()
            .count { it.value > 1 }
    }

    override fun solvePart2(): Int {
        return input.asSequence()
            .flatMap { it.pointSequence() }
            .groupingBy { it }
            .eachCount()
            .count { it.value > 1 }
    }

    private class VentureLine(
        val start: Point2D,
        val end: Point2D,
    ) {
        private val xRange
            get() = IntProgression.fromClosedRange(
                rangeStart = start.x,
                rangeEnd = end.x,
                step = if (start.x > end.x) -1 else 1,
            )
        private val yRange
            get() = IntProgression.fromClosedRange(
                rangeStart = start.y,
                rangeEnd = end.y,
                step = if (start.y > end.y) -1 else 1,
            )

        fun pointSequence(): Sequence<Point2D> = sequence {
            if (start.x == end.x) {
                yRange.forEach { y ->
                    yield(Point2D(start.x, y))
                }
            } else if (start.y == end.y) {
                xRange.forEach { x ->
                    yield(Point2D(x, start.y))
                }
            } else if ((start.x - end.x).absoluteValue == (start.y - end.y).absoluteValue) {
                xRange.zip(yRange).forEach { (x, y) ->
                    yield(Point2D(x, y))
                }
            } else {
                error("we can only create point sequence for vertical, horizontal and diagonal lines")
            }
        }
    }
}
