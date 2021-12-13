package solutions

import utils.Point2D
import java.io.BufferedReader

class Day13(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: Input by lazy {
        val dots = mutableSetOf<Point2D>()
        val folds = mutableListOf<Fold>()

        inputReader.transformLines { line ->
            if (line.startsWith("fold along x=")) {
                folds += Fold.Horizontal(x = line.substringAfter("fold along x=").toInt())
            } else if (line.startsWith("fold along y=")) {
                folds += Fold.Vertical(y = line.substringAfter("fold along y=").toInt())
            } else if (line.isNotBlank()) {
                val (x, y) = line.split(",").map { it.toInt() }
                dots += Point2D(x, y)
            }
        }

        Input(dots, folds)
    }

    override fun solvePart1(): Int {
        return input.dots
            .foldPaper(input.folds.first())
            .size
    }

    override fun solvePart2(): Int {
        val foldedPaper = input.folds
            .fold(input.dots) { dots, fold ->
                dots.foldPaper(fold)
            }
        printPaper(foldedPaper)
        return 0
    }

    private fun Set<Point2D>.foldPaper(fold: Fold): Set<Point2D> {
        val newPoints = mutableSetOf<Point2D>()

        when (fold) {
            is Fold.Horizontal -> forEach { dot ->
                if (dot.x < fold.x) {
                    newPoints.add(dot)
                } else if (dot.x > fold.x) {
                    newPoints.add(dot.copy(x = 2*fold.x - dot.x))
                }
            }
            is Fold.Vertical -> forEach { dot ->
                if (dot.y < fold.y) {
                    newPoints.add(dot)
                } else if (dot.y > fold.y) {
                    newPoints.add(dot.copy(y = 2*fold.y - dot.y))
                }
            }
        }

        return newPoints
    }

    private fun printPaper(dots: Set<Point2D>) {
        val maxX = dots.maxOf { it.x }
        val maxY = dots.maxOf { it.y }
        for (y in 0..maxY) {
            for (x in 0.. maxX) {
                if (Point2D(x, y) in dots) {
                    print('X')
                } else {
                    print(' ')
                }
            }
            println()
        }
    }

    private class Input(
        val dots: Set<Point2D>,
        val folds: List<Fold>,
    )

    private sealed class Fold {
        class Vertical(val y: Int): Fold()
        class Horizontal(val x: Int): Fold()
    }
}
