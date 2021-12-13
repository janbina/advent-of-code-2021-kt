package solutions

import java.io.BufferedReader

class Day12(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val caveMap: Map<String, Set<String>> by lazy {
        val map: MutableMap<String, MutableSet<String>> = mutableMapOf()

        inputReader.transformLines { line ->
            val (a, b) = line.split("-")
            map.getOrPut(a) { mutableSetOf() }.add(b)
            map.getOrPut(b) { mutableSetOf() }.add(a)
        }

        map
    }

    override fun solvePart1(): Int {
        return findAllPathsThroughCaves(::canEnterCaveP1)
    }

    override fun solvePart2(): Int {
        return findAllPathsThroughCaves(::canEnterCaveP2)
    }

    private fun findAllPathsThroughCaves(canEnterCave: (List<String>, String) -> Boolean): Int {
        var numPaths = 0

        val pathStack = mutableListOf<List<String>>()
        pathStack.add(listOf("start"))

        while (pathStack.isNotEmpty()) {
            val path = pathStack.removeLast()
            val nextCaves = caveMap.getOrElse(path.last()) { emptyList() }
            nextCaves.forEach { nextCave ->
                if (nextCave == "end") {
                    numPaths++
                } else if (canEnterCave(path, nextCave)) {
                    pathStack.add(path + nextCave)
                }
            }
        }

        return numPaths
    }

    private fun canEnterCaveP1(path: List<String>, cave: String): Boolean {
        if (cave == "start" || cave == "end") return false
        if (cave.isSmallCave() && cave in path) return false
        return true
    }

    private fun canEnterCaveP2(path: List<String>, cave: String): Boolean {
        if (cave == "start" || cave == "end") return false
        if (cave.isSmallCave() && cave in path && path.hasSmallCaveTwice()) return false
        return true
    }

    private fun String.isSmallCave(): Boolean = all { it.isLowerCase() }

    private fun List<String>.hasSmallCaveTwice(): Boolean {
        val smallCaves = filter { it.isSmallCave() }
        return smallCaves.size > smallCaves.toSet().size
    }
}
