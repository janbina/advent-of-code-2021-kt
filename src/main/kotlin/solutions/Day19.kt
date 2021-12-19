package solutions

import utils.Point3D
import utils.uniquePairs
import java.io.BufferedReader

class Day19(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Scanner> by lazy {
        val scanners = mutableListOf<Scanner>()
        var scannerId = -1
        var beacons = mutableListOf<Point3D>()
        inputReader.forEachLine { line ->
            if (line.startsWith("--- scanner")) {
                if (beacons.isNotEmpty()) {
                    scanners.add(Scanner(scannerId, beacons))
                }
                beacons = mutableListOf()
                scannerId = line
                    .substringAfter("--- scanner ")
                    .substringBefore(" ---")
                    .toInt()
            } else {
                val splits = line.split(",")
                if (splits.size == 3) {
                    val (x, y, z) = splits.map { it.toInt() }
                    beacons.add(Point3D(x, y, z))
                }
            }
        }
        if (beacons.isNotEmpty()) {
            scanners.add(Scanner(scannerId, beacons))
        }
        scanners
    }

    private val alignments: Map<Int, Alignment> by lazy { findAlignments() }

    override fun solvePart1(): Int {
        val uniqueBeacons = input.flatMap { scanner ->
            val alignment = alignments[scanner.id] ?: error("No alignment for scanner ${scanner.id}")
            scanner.beacons.map { alignment.mapper(it) + alignment.shift }
        }.toSet()

        return uniqueBeacons.size
    }

    override fun solvePart2(): Int {
        val shifts = alignments.values.map { it.shift }

        return shifts.uniquePairs().maxOf { (a, b) -> a.manhattanDistanceTo(b) }
    }

    private fun findAlignments(): Map<Int, Alignment> {
        val alignedWithFirst = mutableListOf(input.first { it.id == 0 })
        val notYetAligned = input.filter { it.id != 0 }.toMutableList()
        val alignments = mutableMapOf(
            0 to Alignment({it}, Point3D(0, 0, 0))
        )
        val allScannerIds = input.map { it.id }
        val possibleAlignments = notYetAligned.map { it.id }.associateWith { id -> allScannerIds.toMutableSet().also { it.remove(id) } }

        outer@ while (notYetAligned.isNotEmpty()) {
            for (i in notYetAligned.indices) {
                val toAlign = notYetAligned[i]
                val alignment = findAlignment(
                    aligned = alignedWithFirst.filter { it.id in possibleAlignments.getOrElse(toAlign.id) { emptySet() } },
                    toAlign = toAlign,
                )
                if (alignment != null) {
                    println("Found alignment for ${toAlign.id}")
                    notYetAligned.removeAt(i)
                    alignedWithFirst.add(toAlign.align(alignment))
                    alignments[toAlign.id] = alignment
                    continue@outer
                } else {
                    possibleAlignments[toAlign.id]?.removeAll(alignedWithFirst.map { it.id })
                }
            }
            error("cannot find new alignment")
        }

        return alignments
    }

    private fun findAlignment(aligned: List<Scanner>, toAlign: Scanner): Alignment? {
        aligned.forEach { alignedScanner ->
            beaconMappers.forEach { mapper ->
                val mapped = toAlign.beacons.map(mapper)
                val shift = findShift(alignedScanner.beacons, mapped)
                if (shift != null) return Alignment(mapper, shift)
            }
        }
        return null
    }

    private fun findShift(
        aligned: List<Point3D>,
        toAlign: List<Point3D>,
    ): Point3D? {
        val alignedSet = aligned.toSet()
        // get all pairs, find shift to align them and check how many beacons match
        for (a in aligned) {
            for (b in toAlign) {
                val shift = a - b
                val shifted = toAlign.map { it + shift }.toSet()
                val all = alignedSet + shifted
                if (alignedSet.size + shifted.size - all.size >= 12) {
                    return shift
                }
            }
        }
        return null
    }

    private fun Scanner.align(alignment: Alignment): Scanner {
        return Scanner(
            id = id,
            beacons = beacons.map { alignment.mapper(it) + alignment.shift },
        )
    }

    private class Scanner(
        val id: Int,
        val beacons: List<Point3D>,
    )

    private class Alignment(
        val mapper: (Point3D) -> Point3D,
        val shift: Point3D,
    )

    private val beaconMappers = listOf<(Point3D) -> Point3D>(
        { Point3D(it.x, it.y, it.z) },
        { Point3D(it.y, -it.x, it.z) },
        { Point3D(-it.x, -it.y, it.z) },
        { Point3D(-it.y, it.x, it.z) },

        { Point3D(-it.x, it.y, -it.z) },
        { Point3D(it.y, it.x, -it.z) },
        { Point3D(it.x, -it.y, -it.z) },
        { Point3D(-it.y, -it.x, -it.z) },

        { Point3D(it.x, -it.z, it.y) },
        { Point3D(-it.z, -it.x, it.y) },
        { Point3D(-it.x, it.z, it.y) },
        { Point3D(it.z, it.x, it.y) },

        { Point3D(-it.x, -it.z, -it.y) },
        { Point3D(-it.z, it.x, -it.y) },
        { Point3D(it.x, it.z, -it.y) },
        { Point3D(it.z, -it.x, -it.y) },

        { Point3D(-it.y, -it.z, it.x) },
        { Point3D(-it.z, it.y, it.x) },
        { Point3D(it.y, it.z, it.x) },
        { Point3D(it.z, -it.y, it.x) },

        { Point3D(it.y, -it.z, -it.x) },
        { Point3D(-it.z, -it.y, -it.x) },
        { Point3D(-it.y, it.z, -it.x) },
        { Point3D(it.z, it.y, -it.x) },
    )
}
