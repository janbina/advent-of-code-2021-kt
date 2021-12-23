package solutions

import utils.Point2D
import java.io.BufferedReader

class Day23(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val targetChamber = mapOf('A' to 0, 'B' to 1, 'C' to 2, 'D' to 3)
    private val energyToMove = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)
    private val burrowMap = createMap()
    private val initialConfiguration = createInitialConfiguration(inputReader)

    override fun solvePart1(): Int {
        println(setOf("a", "b") == setOf("b", "a"))
        return 1
//        return findAllFinalConfigurations(initialConfiguration, burrowMap).onEach {
//            println("found conf")
//            burrowMap.print(it)
//        }.minOf { it.energyUsed }
    }

    override fun solvePart2(): Int {
        return 2
    }

    private fun findAllFinalConfigurations(
        initial: Configuration,
        map: BurrowMap,
    ): Sequence<Configuration> = sequence {
        val queue = ArrayDeque<Configuration>()
        queue.add(initial)

        while (queue.isNotEmpty()) {
            val top = queue.removeFirst()
//            if (top.energyUsed < 1000) {
//                println("TOP:")
//                map.print(top)
//            }
            if (top.isFinal(map)) {
                yield(top)
            } else {
                top.findPossibleMoves(map).forEach { move ->
                    val new = top.move(move.first, move.second)
//                    if (top.energyUsed < 1000) {
//                        println("NEW:")
//                        map.print(new)
//                    }
                    queue.add(new)
                }
            }
        }
    }

    private fun createMap(): BurrowMap {
        // 0123456789X
        //#############
        //#...........#  0
        //###1#2#3#4###  1
        //__#1#2#3#4#    2
        //__#########
        val map = mutableMapOf<Point2D, FieldType>()
        val roomXIndices = setOf(2, 4, 6, 8)
        roomXIndices.forEachIndexed { index, x ->
            map[Point2D(x, 1)] = FieldType.Room(index)
            map[Point2D(x, 2)] = FieldType.Room(index)
        }
        (0..10).forEach { x ->
            map[Point2D(x, 0)] = FieldType.Hallway(x !in roomXIndices)
        }
        return BurrowMap(map)
    }

    private fun createInitialConfiguration(inputReader: BufferedReader): Configuration {
        val amphipods = mutableMapOf<Point2D, Amphipod>()
        inputReader.readLines().drop(1).map { it.drop(1) }.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (c.isUpperCase()) {
                    amphipods[Point2D(x, y)] = Amphipod(
                        id = c,
                        targetChamber = targetChamber[c]
                            ?: error("No target chamber for amphipod \"$c\""),
                        energyToMove = energyToMove[c]
                            ?: error("No move energy for amphipod \"$c\""),
                    )
                }
            }
        }
        return Configuration(amphipods, 0)
    }

    private sealed class FieldType {
        data class Hallway(
            val canStopHere: Boolean,
        ) : FieldType()

        data class Room(
            val id: Int,
        ) : FieldType()
    }

    private data class Amphipod(
        val id: Char,
        val targetChamber: Int,
        val energyToMove: Int,
    )

    private data class Configuration(
        val amphipods: Map<Point2D, Amphipod>,
        val energyUsed: Int,
    ) {
        fun isFinal(map: BurrowMap): Boolean {
            return amphipods.all { (position, amphipod) ->
                val field = map.fields[position]
                field != null && field is FieldType.Room && field.id == amphipod.targetChamber
            }
        }

        fun findPossibleMoves(map: BurrowMap): List<Pair<Amphipod, List<Point2D>>> {
            val moves = mutableListOf<Pair<Amphipod, List<Point2D>>>()
            amphipods.forEach { (pos, amph) ->
                val targets = amph.possibleTargets(pos, map)
                targets.forEach { target ->
                    val path = map.shortestPath(pos, target)
                    if (path != null && path.drop(1).all { it !in amphipods }) {
                        moves.add(amph to path)
                    }
                }
            }
            return moves
        }

        fun move(amphipod: Amphipod, path: List<Point2D>): Configuration {
            return Configuration(
                amphipods = amphipods.toMutableMap().apply {
                    remove(path.first())
                    set(path.last(), amphipod)
                },
                energyUsed = energyUsed + path.size * amphipod.energyToMove,
            )
        }

        private fun Amphipod.possibleTargets(position: Point2D, map: BurrowMap): Set<Point2D> {
            val field = map.fields[position]!!
            val targetRooms = map.fields.filterValues { it is FieldType.Room && it.id == targetChamber }.keys
            val hallway = map.fields.filterValues { it is FieldType.Hallway && it.canStopHere }.keys
            return if (field is FieldType.Room) {
                targetRooms + hallway
            } else {
                targetRooms
            }
        }
    }

    private class BurrowMap(
        val fields: Map<Point2D, FieldType>,
    ) {
        fun shortestPath(from: Point2D, to: Point2D): List<Point2D>? {
            val queue = ArrayDeque<List<Point2D>>()
            val visited = mutableSetOf<Point2D>()
            queue.add(listOf(from))
            visited.add(from)

            while (queue.isNotEmpty()) {
                val top = queue.removeFirst()
                if (top.last() == to) return top
                top.last().neighbors()
                    .filter { it !in visited && it in fields }
                    .forEach {
                        queue.add(top + it)
                        visited.add(it)
                    }
            }

            return null
        }

        fun print(config: Configuration) {
            val xR = (fields.minOf { it.key.x } - 1)..(fields.maxOf { it.key.x } + 1)
            val yR = (fields.minOf { it.key.y } - 1)..(fields.maxOf { it.key.y } + 1)
            println()
            for (y in yR) {
                for (x in xR) {
                    val point = Point2D(x, y)
                    val amph = config.amphipods[point]
                    val field = fields[point]
                    val c = when {
                        amph != null -> amph.id
                        field != null -> '.'
                        else -> '#'
                    }
                    print(c)
                }
                println()
            }
        }
    }
}
