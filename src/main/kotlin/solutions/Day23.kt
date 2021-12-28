package solutions

import utils.AStarResult
import utils.Point2D
import utils.aStarSearch
import utils.sumOf
import java.io.BufferedReader
import kotlin.math.absoluteValue

class Day23(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val inputLines = inputReader.readLines()

    override fun solvePart1(): Int {
        val burrowMap = BurrowMap(
            hallway = listOf(0, 1, 3, 5, 7, 9, 10).map { Point2D(it, 0) }.toSet(),
            preRoom = listOf(2, 4, 6, 8).map { Point2D(it, 0) }.toSet(),
            amberRoom = (1..2).map { Point2D(2, it) }.toSet(),
            bronzeRoom = (1..2).map { Point2D(4, it) }.toSet(),
            copperRoom = (1..2).map { Point2D(6, it) }.toSet(),
            dessertRoom = (1..2).map { Point2D(8, it) }.toSet(),
        )
        val initial = createInitialConfiguration(inputLines)
        val target = createTargetConfiguration(burrowMap)

        burrowMap.print(initial)
        println()
        burrowMap.print(target)

        val result = solve(burrowMap, initial, target)

        println("SOLUTION:")
        result?.path?.forEach {
            burrowMap.print(it)
            println()
        }

        return result?.cost ?: Int.MAX_VALUE
    }

    override fun solvePart2(): Int {
        val burrowMap = BurrowMap(
            hallway = listOf(0, 1, 3, 5, 7, 9, 10).map { Point2D(it, 0) }.toSet(),
            preRoom = listOf(2, 4, 6, 8).map { Point2D(it, 0) }.toSet(),
            amberRoom = (1..4).map { Point2D(2, it) }.toSet(),
            bronzeRoom = (1..4).map { Point2D(4, it) }.toSet(),
            copperRoom = (1..4).map { Point2D(6, it) }.toSet(),
            dessertRoom = (1..4).map { Point2D(8, it) }.toSet(),
        )
        val initial = createInitialConfiguration(
            inputLines.toMutableList().apply {
                add(3, "  #D#C#B#A#")
                add(4, "  #D#B#A#C#")
            }
        )
        val target = createTargetConfiguration(burrowMap)

        burrowMap.print(initial)
        println()
        burrowMap.print(target)

        val result = solve(burrowMap, initial, target)

        println("SOLUTION:")
        result?.path?.forEach {
            burrowMap.print(it)
            println()
        }

        return result?.cost ?: Int.MAX_VALUE
    }

    private fun createInitialConfiguration(lines: List<String>): Configuration {
        val amphipods = mutableMapOf<Point2D, Amphipod>()

        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, char ->
                if (char.isUpperCase()) {
                    amphipods[Point2D(x - 1, y - 1)] = Amphipod.fromChar(char)
                }
            }
        }

        return Configuration(amphipods)
    }

    private fun createTargetConfiguration(burrowMap: BurrowMap): Configuration {
        val amphipods = mutableMapOf<Point2D, Amphipod>()
        amphipods.putAll(burrowMap.amberRoom.associateWith { Amphipod.Amber })
        amphipods.putAll(burrowMap.bronzeRoom.associateWith { Amphipod.Bronze })
        amphipods.putAll(burrowMap.copperRoom.associateWith { Amphipod.Copper })
        amphipods.putAll(burrowMap.dessertRoom.associateWith { Amphipod.Dessert })
        return Configuration(amphipods)
    }

    private fun solve(
        burrowMap: BurrowMap,
        initialConfiguration: Configuration,
        targetConfiguration: Configuration,
    ): AStarResult<Configuration>? {
        return aStarSearch(
            start = initialConfiguration,
            end = targetConfiguration,
            next = { next(it, burrowMap).asIterable() },
            heuristicCostToEnd = { heuristicToEnd(it, burrowMap) },
        )
    }

    private fun next(
        configuration: Configuration,
        burrowMap: BurrowMap
    ): Sequence<Pair<Configuration, Int>> {
        return burrowMap.nextConfigurations(configuration)
    }

    private fun heuristicToEnd(configuration: Configuration, burrowMap: BurrowMap): Int {
        return configuration.amphipods.sumOf { (point, amphipod) ->
            heuristicSteps(point, burrowMap.roomFor(amphipod)) * amphipod.moveEnergy
        }
    }

    private fun heuristicSteps(point: Point2D, room: Set<Point2D>): Int {
        val roomX = room.first().x
        if (point.x == roomX) return 0
        if (point.y == 0) return 2
        return (point.x - roomX).absoluteValue + point.y + 1
    }

    private class BurrowMap(
        val hallway: Set<Point2D>,
        val preRoom: Set<Point2D>,
        val amberRoom: Set<Point2D>,
        val bronzeRoom: Set<Point2D>,
        val copperRoom: Set<Point2D>,
        val dessertRoom: Set<Point2D>,
    ) {
        val all = hallway + preRoom + amberRoom + bronzeRoom + copperRoom + dessertRoom
        val minY = all.minOf { it.y }
        val maxY = all.maxOf { it.y }
//
//        fun neighborsOf(point: Point2D): List<Point2D> = point.neighbors().filter { it in all }

        fun roomFor(amphipod: Amphipod): Set<Point2D> = when (amphipod) {
            Amphipod.Amber -> amberRoom
            Amphipod.Bronze -> bronzeRoom
            Amphipod.Copper -> copperRoom
            Amphipod.Dessert -> dessertRoom
        }

        fun path(from: Point2D, to: Point2D) = generateSequence(from) { current ->
            when {
                current == to -> null
                current.y == 0 && current.x < to.x -> Point2D(current.x + 1, 0)
                current.y == 0 && current.x > to.x -> Point2D(current.x - 1, 0)
                current.x != to.x -> Point2D(current.x, current.y - 1)
                current.x == to.x -> Point2D(current.x, current.y + 1)
                else -> error("unreachable")
            }
        }

        fun nextConfigurations(
            configuration: Configuration
        ): Sequence<Pair<Configuration, Int>> = sequence {
            for ((position, amphipod) in configuration.amphipods) {
                val targetRoom = roomFor(amphipod)
                val roomContent = configuration.amphipods.filter { it.key in targetRoom }

                if (position in targetRoom && roomContent.all { it.value == amphipod }) {
                    // amphipod is in its room and doesn't have to leave as there is no
                    // amphipod of different type there
                    continue
                }

                // here, amphipod is either in hallway, different room or his room but he needs
                // to move to let others out...

                // first try if we can move it directly to its room:
                if (position !in targetRoom && roomContent.all { it.value == amphipod }) {
                    // target is the bottom of target room
                    val target = targetRoom.maxByOrNull { if (it in roomContent) -1 else it.y }!!
                    require(target.y > 0)
                    if (yieldIfPossible(position, target, amphipod, configuration)) {
                        continue
                    }
                }

                // move to hallway
                if (position !in hallway) {
                    hallway.forEach { target ->
                        yieldIfPossible(position, target, amphipod, configuration)
                    }
                }
            }
        }

        private suspend fun SequenceScope<Pair<Configuration, Int>>.yieldIfPossible(
            from: Point2D,
            to: Point2D,
            amphipod: Amphipod,
            configuration: Configuration,
        ): Boolean {
            val path = path(from, to).drop(1)
            if (path.any { it in configuration.amphipods }) return false // path must be free
            yield(configuration.withMoved(from, to) to path.count() * amphipod.moveEnergy)
            return true
        }

        fun print(configuration: Configuration) {
            println("#############")
            (minY..maxY).forEach { y ->
                (-1..11).forEach { x ->
                    val amph = configuration.amphipods[Point2D(x, y)]
                    when {
                        amph != null -> print(amph.char)
                        Point2D(x, y) in all -> print('.')
                        else -> print('#')
                    }
                }
                println()
            }
            println("#############")
        }
    }

    private data class Configuration(
        val amphipods: Map<Point2D, Amphipod>,
    ) {
        fun withMoved(from: Point2D, to: Point2D): Configuration {
            return Configuration(
                amphipods.toMutableMap().apply {
                    this[to] = this[from] ?: error("No amphipod on from position")
                    remove(from)
                }
            )
        }
    }

    enum class Amphipod(val moveEnergy: Int, val char: Char) {
        Amber(1, 'A'),
        Bronze(10, 'B'),
        Copper(100, 'C'),
        Dessert(1000, 'D');

        companion object {
            fun fromChar(c: Char) =
                values().firstOrNull { it.char == c } ?: error("Unknown amphipod")
        }
    }
}
