package solutions

import utils.Point2D
import utils.aStarSearch
import java.io.BufferedReader
import kotlin.math.absoluteValue

class Day23(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val inputLines = inputReader.readLines()

    override fun solvePart1(): Int {
        val burrowMap = BurrowMap(
            hallway = (0..10).map { Point2D(it, 0) }.toSet(),
            amberRoom = (1..2).map { Point2D(2, it) }.toSet(),
            bronzeRoom = (1..2).map { Point2D(4, it) }.toSet(),
            copperRoom = (1..2).map { Point2D(6, it) }.toSet(),
            dessertRoom = (1..2).map { Point2D(8, it) }.toSet(),
        )
        val initial = createInitialConfiguration(inputLines)
        val target = createTargetConfiguration(burrowMap)

        return solve(burrowMap, initial, target)
    }

    override fun solvePart2(): Int {
        return 2
    }

    private fun createInitialConfiguration(lines: List<String>): Configuration {
        val amber = mutableSetOf<Point2D>()
        val bronze = mutableSetOf<Point2D>()
        val copper = mutableSetOf<Point2D>()
        val desert = mutableSetOf<Point2D>()

        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, char ->
                when (char) {
                    'A' -> amber.add(Point2D(x - 1, y - 1))
                    'B' -> bronze.add(Point2D(x - 1, y - 1))
                    'C' -> copper.add(Point2D(x - 1, y - 1))
                    'D' -> desert.add(Point2D(x - 1, y - 1))
                }
            }
        }

        return Configuration(amber, bronze, copper, desert)
    }

    private fun createTargetConfiguration(burrowMap: BurrowMap): Configuration {
        return Configuration(
            burrowMap.amberRoom,
            burrowMap.bronzeRoom,
            burrowMap.copperRoom,
            burrowMap.dessertRoom,
        )
    }

    private fun solve(
        burrowMap: BurrowMap,
        initialConfiguration: Configuration,
        targetConfiguration: Configuration,
    ): Int {
        return aStarSearch(
            start = initialConfiguration,
            end = targetConfiguration,
            next = { next(it, burrowMap) },
            heuristicCostToEnd = { heuristicToEnd(it, burrowMap) },
        )?.cost ?: Int.MAX_VALUE
    }

    private fun next(configuration: Configuration, burrowMap: BurrowMap): List<Pair<Configuration, Int>> {
        val list = mutableListOf<Pair<Configuration, Int>>()
        return configuration.a.flatMap {
            burrowMap.neighborsOf(it)
        }.filter { it !in configuration.all }.map { it to 1 }
    }

    private fun heuristicToEnd(configuration: Configuration, burrowMap: BurrowMap): Int {
        return configuration.amber.sumOf { heuristicSteps(it, burrowMap.amberRoom) } +
                configuration.bronze.sumOf { heuristicSteps(it, burrowMap.bronzeRoom) } * 10 +
                configuration.copper.sumOf { heuristicSteps(it, burrowMap.copperRoom) } * 100 +
                configuration.dessert.sumOf { heuristicSteps(it, burrowMap.dessertRoom) } * 1000
    }

    private fun heuristicSteps(amphipod: Point2D, room: Set<Point2D>): Int {
        val roomX = room.first().x
        if (amphipod.x == roomX) return 0
        return (amphipod.x - roomX).absoluteValue + amphipod.y + 1
    }

    private class BurrowMap(
        val hallway: Set<Point2D>,
        val amberRoom: Set<Point2D>,
        val bronzeRoom: Set<Point2D>,
        val copperRoom: Set<Point2D>,
        val dessertRoom: Set<Point2D>,
    ) {
        val all = hallway + amberRoom + bronzeRoom + copperRoom + dessertRoom

        fun neighborsOf(point: Point2D): List<Point2D> = point.neighbors().filter { it in all }
    }

    private data class Configuration(
        val amber: Set<Point2D>,
        val bronze: Set<Point2D>,
        val copper: Set<Point2D>,
        val dessert: Set<Point2D>,
    ) {
        val all = amber + bronze + copper + dessert

        fun withMoved(now: Point2D, after: Point2D): Configuration? {
            if (after in all) return null

        }
    }

    enum class Amphipod(val moveEnergy: Int) {
        Amber(1),
        Bronze(10),
        Copper(100),
        Dessert(1000);

        companion object {
            fun fromChar(c: Char) = when (c) {
                'A' -> Amber
                'B' -> Bronze
                'C' -> Copper
                'D' -> Dessert
                else -> error("Unknown amphipod")
            }
        }
    }


//    private data class
//
//    private val targetChamber = mapOf('A' to 0, 'B' to 1, 'C' to 2, 'D' to 3)
//    private val energyToMove = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)
//    private val burrowMap = createMap()
//    private val initialConfiguration = createInitialConfiguration(inputReader)
//
//    override fun solvePart1(): Int {
//        println(setOf("a", "b") == setOf("b", "a"))
//        return 1
////        return findAllFinalConfigurations(initialConfiguration, burrowMap).onEach {
////            println("found conf")
////            burrowMap.print(it)
////        }.minOf { it.energyUsed }
//    }
//
//    override fun solvePart2(): Int {
//        return 2
//    }
//
//    private fun findAllFinalConfigurations(
//        initial: Configuration,
//        map: BurrowMap,
//    ): Sequence<Configuration> = sequence {
//        val queue = ArrayDeque<Configuration>()
//        queue.add(initial)
//
//        while (queue.isNotEmpty()) {
//            val top = queue.removeFirst()
////            if (top.energyUsed < 1000) {
////                println("TOP:")
////                map.print(top)
////            }
//            if (top.isFinal(map)) {
//                yield(top)
//            } else {
//                top.findPossibleMoves(map).forEach { move ->
//                    val new = top.move(move.first, move.second)
////                    if (top.energyUsed < 1000) {
////                        println("NEW:")
////                        map.print(new)
////                    }
//                    queue.add(new)
//                }
//            }
//        }
//    }
//
//    private fun createMap(): BurrowMap {
//        // 0123456789X
//        //#############
//        //#...........#  0
//        //###1#2#3#4###  1
//        //__#1#2#3#4#    2
//        //__#########
//        val map = mutableMapOf<Point2D, FieldType>()
//        val roomXIndices = setOf(2, 4, 6, 8)
//        roomXIndices.forEachIndexed { index, x ->
//            map[Point2D(x, 1)] = FieldType.Room(index)
//            map[Point2D(x, 2)] = FieldType.Room(index)
//        }
//        (0..10).forEach { x ->
//            map[Point2D(x, 0)] = FieldType.Hallway(x !in roomXIndices)
//        }
//        return BurrowMap(map)
//    }
//
//    private fun createInitialConfiguration(inputReader: BufferedReader): Configuration {
//        val amphipods = mutableMapOf<Point2D, Amphipod>()
//        inputReader.readLines().drop(1).map { it.drop(1) }.forEachIndexed { y, line ->
//            line.forEachIndexed { x, c ->
//                if (c.isUpperCase()) {
//                    amphipods[Point2D(x, y)] = Amphipod(
//                        id = c,
//                        targetChamber = targetChamber[c]
//                            ?: error("No target chamber for amphipod \"$c\""),
//                        energyToMove = energyToMove[c]
//                            ?: error("No move energy for amphipod \"$c\""),
//                    )
//                }
//            }
//        }
//        return Configuration(amphipods, 0)
//    }
//
//    private sealed class FieldType {
//        data class Hallway(
//            val canStopHere: Boolean,
//        ) : FieldType()
//
//        data class Room(
//            val id: Int,
//        ) : FieldType()
//    }
//
//    private data class Amphipod(
//        val id: Char,
//        val targetChamber: Int,
//        val energyToMove: Int,
//    )
//
//    private data class Configuration(
//        val amphipods: Map<Point2D, Amphipod>,
//        val energyUsed: Int,
//    ) {
//        fun isFinal(map: BurrowMap): Boolean {
//            return amphipods.all { (position, amphipod) ->
//                val field = map.fields[position]
//                field != null && field is FieldType.Room && field.id == amphipod.targetChamber
//            }
//        }
//
//        fun findPossibleMoves(map: BurrowMap): List<Pair<Amphipod, List<Point2D>>> {
//            val moves = mutableListOf<Pair<Amphipod, List<Point2D>>>()
//            amphipods.forEach { (pos, amph) ->
//                val targets = amph.possibleTargets(pos, map)
//                targets.forEach { target ->
//                    val path = map.shortestPath(pos, target)
//                    if (path != null && path.drop(1).all { it !in amphipods }) {
//                        moves.add(amph to path)
//                    }
//                }
//            }
//            return moves
//        }
//
//        fun move(amphipod: Amphipod, path: List<Point2D>): Configuration {
//            return Configuration(
//                amphipods = amphipods.toMutableMap().apply {
//                    remove(path.first())
//                    set(path.last(), amphipod)
//                },
//                energyUsed = energyUsed + path.size * amphipod.energyToMove,
//            )
//        }
//
//        private fun Amphipod.possibleTargets(position: Point2D, map: BurrowMap): Set<Point2D> {
//            val field = map.fields[position]!!
//            val targetRooms = map.fields.filterValues { it is FieldType.Room && it.id == targetChamber }.keys
//            val hallway = map.fields.filterValues { it is FieldType.Hallway && it.canStopHere }.keys
//            return if (field is FieldType.Room) {
//                targetRooms + hallway
//            } else {
//                targetRooms
//            }
//        }
//    }
//
//    private class BurrowMap(
//        val fields: Map<Point2D, FieldType>,
//    ) {
//        fun shortestPath(from: Point2D, to: Point2D): List<Point2D>? {
//            val queue = ArrayDeque<List<Point2D>>()
//            val visited = mutableSetOf<Point2D>()
//            queue.add(listOf(from))
//            visited.add(from)
//
//            while (queue.isNotEmpty()) {
//                val top = queue.removeFirst()
//                if (top.last() == to) return top
//                top.last().neighbors()
//                    .filter { it !in visited && it in fields }
//                    .forEach {
//                        queue.add(top + it)
//                        visited.add(it)
//                    }
//            }
//
//            return null
//        }
//
//        fun print(config: Configuration) {
//            val xR = (fields.minOf { it.key.x } - 1)..(fields.maxOf { it.key.x } + 1)
//            val yR = (fields.minOf { it.key.y } - 1)..(fields.maxOf { it.key.y } + 1)
//            println()
//            for (y in yR) {
//                for (x in xR) {
//                    val point = Point2D(x, y)
//                    val amph = config.amphipods[point]
//                    val field = fields[point]
//                    val c = when {
//                        amph != null -> amph.id
//                        field != null -> '.'
//                        else -> '#'
//                    }
//                    print(c)
//                }
//                println()
//            }
//        }
//    }
}
