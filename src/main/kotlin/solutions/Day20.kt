package solutions

import utils.Move
import utils.Point2D
import java.io.BufferedReader

class Day20(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: Input by lazy {
        val lines = inputReader.readLines()

        val litPixels = mutableSetOf<Point2D>()
        lines.drop(2).forEachIndexed { y, imgLine ->
            imgLine.forEachIndexed { x, pixel ->
                if (pixel == '#') {
                    litPixels.add(Point2D(x, y))
                }
            }
        }
        val maxX = lines.drop(2).maxOf { it.lastIndex }
        val maxY = lines.size - 3

        Input(
            enhancement = Enhancement(lines.first().map { it == '#' }),
            image = Image(
                litPixels = litPixels,
                topLeft = Point2D(0, 0),
                bottomRight = Point2D(maxX, maxY),
                infinitePixelsAreLit = false,
            ),
        )
    }

    override fun solvePart1(): Int {
        val enhancement = input.enhancement
        val twiceEnhanced = input.image.enhanced(enhancement).enhanced(enhancement)
        return twiceEnhanced.getLitPixelsCount()
    }

    override fun solvePart2(): Int {
        val enhancement = input.enhancement
        val enhanced50times = (0 until 50).fold(input.image) { img, _ -> img.enhanced(enhancement) }
        return enhanced50times.getLitPixelsCount()
    }

    private class Input(
        val enhancement: Enhancement,
        val image: Image,
    )

    private class Enhancement(
        private val shouldLit: List<Boolean>,
    ) {
        fun shouldLit(pixelSquare: List<Boolean>): Boolean {
            require(pixelSquare.size == 9)
            val index = pixelSquare.joinToString(separator = "") { if (it) "1" else "0" }.toInt(2)
            return shouldLit[index]
        }
    }

    private class Image(
        private val litPixels: Set<Point2D>,
        private val topLeft: Point2D,
        private val bottomRight: Point2D,
        private val infinitePixelsAreLit: Boolean,
    ) {

        private val xRange = topLeft.x..bottomRight.x
        private val yRange = topLeft.y..bottomRight.y

        fun getLitPixelsCount(): Int {
            if (infinitePixelsAreLit) return Int.MAX_VALUE
            return litPixels.size
        }

        fun enhanced(enhancement: Enhancement): Image {
            val newTopLeft = topLeft.upLeft()
            val newBottomRight = bottomRight.downRight()
            val newXRange = newTopLeft.x..newBottomRight.x
            val newYRange = newTopLeft.y..newBottomRight.y
            val newLitPixels = (newXRange).flatMap { x ->
                newYRange.map { y -> Point2D(x, y) }
            }.filter { pixel ->
                enhancement.shouldLit(
                    pixelSquare = pixel.pixelsSquare().map { it.isLit() }
                )
            }.toSet()
            val newInfinitePixelsAreLit = if (infinitePixelsAreLit) {
                enhancement.shouldLit((0..8).map { true })
            } else {
                enhancement.shouldLit((0..8).map { false })
            }
            return Image(
                litPixels = newLitPixels,
                topLeft = newTopLeft,
                bottomRight = newBottomRight,
                infinitePixelsAreLit = newInfinitePixelsAreLit,
            )
        }

        @Suppress("unused")
        fun print() {
            yRange.forEach { y ->
                val line = xRange.map { x ->
                    if (Point2D(x, y).isLit()) '#' else '.'
                }.joinToString("")
                println(line)
            }
        }

        private fun Point2D.isLit(): Boolean {
            if (this in litPixels) return true
            if (x in xRange && y in yRange) return false
            return infinitePixelsAreLit
        }

        private fun Point2D.pixelsSquare(): List<Point2D> {
            return (-1..1).flatMap { y ->
                (-1..1).map { x -> Move(x, y) }
            }.map(::applyMove)
        }
    }
}
