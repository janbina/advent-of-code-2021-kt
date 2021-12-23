package solutions

import utils.Point2D
import utils.aStarSearch
import java.io.BufferedReader

class Day15(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: CaveMap by lazy {
        val map = mutableMapOf<Point2D, Int>()

        inputReader.useLines { lines ->
            lines.forEachIndexed { y, line ->
                line.map { it.digitToInt() }.forEachIndexed { x, risk ->
                    map[Point2D(x, y)] = risk
                }
            }
        }

        val width = map.keys.maxOf { it.x } + 1
        val height = map.keys.maxOf { it.y } + 1

        CaveMap(map, width, height)
    }

    override fun solvePart1(): Int {
        val caveMap = input
        val start = Point2D(0, 0)
        val end = Point2D(caveMap.width - 1, caveMap.height - 1)

        return aStarSearch(
            start = start,
            end = end,
            next = { point ->
                point.neighbors().mapNotNull { neighbor ->
                    caveMap.riskForPoint(neighbor, 1)?.let { risk ->
                        neighbor to risk
                    }
                }
            },
            heuristicCostToEnd = { point ->
                end.x - point.x + end.y - point.y
            },
        )?.cost ?: Int.MAX_VALUE
    }

    override fun solvePart2(): Int {
        val caveMap = input
        val start = Point2D(0, 0)
        val end = Point2D(caveMap.width * 5 - 1, caveMap.height * 5 - 1)

        return aStarSearch(
            start = start,
            end = end,
            next = { point ->
                point.neighbors().mapNotNull { neighbor ->
                    caveMap.riskForPoint(neighbor, 5)?.let { risk ->
                        neighbor to risk
                    }
                }
            },
            heuristicCostToEnd = { point ->
                end.x - point.x + end.y - point.y
            },
        )?.cost ?: Int.MAX_VALUE
    }

    private class CaveMap(
        private val risks: Map<Point2D, Int>,
        val width: Int,
        val height: Int,
    ) {

        fun riskForPoint(point: Point2D, mapDuplication: Int = 1): Int? {
            val origX = point.x % width
            val origY = point.y % height
            val duplicationX = point.x / width
            val duplicationY = point.y / height

            if (duplicationX >= mapDuplication || duplicationY >= mapDuplication) {
                return null
            }

            val baseRisk = risks[Point2D(origX, origY)] ?: return null

            return (baseRisk + duplicationX + duplicationY - 1) % 9 + 1
        }
    }
}
