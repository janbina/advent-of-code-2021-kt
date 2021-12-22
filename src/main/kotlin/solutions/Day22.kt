package solutions

import java.io.BufferedReader

class Day22(
    inputReader: BufferedReader,
) : Day<Long, Long>() {

    private val input: List<Instruction> by lazy {
        inputReader.transformLines { Instruction.fromString(it) }
    }

    override fun solvePart1(): Long {
        val region = Cuboid(-50..50, -50..50, -50..50)
        val filtered = input.mapNotNull { instruction ->
            instruction.cuboid.intersect(region)?.let { intersection ->
                Instruction(instruction.on, intersection)
            }
        }
        return runRebootSteps(filtered)
    }

    override fun solvePart2(): Long {
        return runRebootSteps(input)
    }

    private fun runRebootSteps(steps: Iterable<Instruction>): Long {
        val objectsOn: List<Cuboid> = steps.fold(listOf()) { acc, step -> applyStep(acc, step) }
        return objectsOn.sumOf { it.volume }
    }

    private fun applyStep(objectsOn: List<Cuboid>, step: Instruction): List<Cuboid> {
        return if (step.on) {
            val newObjects = objectsOn.fold(listOf(step.cuboid)) { acc, on ->
                acc.flatMap { it.minus(on) }
            }
            objectsOn + newObjects
        } else {
            objectsOn.flatMap { it.minus(step.cuboid) }
        }
    }

    private data class Instruction(
        val on: Boolean,
        val cuboid: Cuboid,
    ) {

        companion object {
            fun fromString(string: String): Instruction {
                val s = string.split("=", "..", ",")
                return Instruction(
                    on = string.startsWith("on"),
                    cuboid = Cuboid(
                        x = s[1].toInt()..s[2].toInt(),
                        y = s[4].toInt()..s[5].toInt(),
                        z = s[7].toInt()..s[8].toInt(),
                    )
                )
            }
        }
    }

    private data class Cuboid(
        val x: IntRange,
        val y: IntRange,
        val z: IntRange,
    ) {
        val volume: Long get() = x.size.toLong() * y.size * z.size

        fun intersect(other: Cuboid): Cuboid? {
            val nx = x.intersect(other.x)
            val ny = y.intersect(other.y)
            val nz = z.intersect(other.z)
            return if (nx.isNotEmpty() && ny.isNotEmpty() && nz.isNotEmpty()) {
                Cuboid(nx, ny, nz)
            } else null
        }

        fun minus(other: Cuboid): List<Cuboid> {
            val list = mutableListOf<Cuboid>()

            val intersection = intersect(other) ?: return listOf(this)

            x.minus(intersection.x).forEach { nx ->
                list.add(Cuboid(nx, y, z))
            }

            y.minus(intersection.y).forEach { ny ->
                list.add(Cuboid(intersection.x, ny, z))
            }

            z.minus(intersection.z).forEach { nz ->
                list.add(Cuboid(intersection.x, intersection.y, nz))
            }

            return list
        }
    }
}

private val IntRange.size: Int get() = last - first + 1

private fun IntRange.isNotEmpty(): Boolean = isEmpty().not()

private fun IntRange.intersect(other: IntRange): IntRange {
    val first = maxOf(this.first, other.first)
    val last = minOf(this.last, other.last)
    return first..last
}

private fun IntRange.minus(other: IntRange): List<IntRange> {
    if (this.intersect(other).size == this.size) return emptyList()
    if (other.first >= this.first && other.last <= this.last) {
        return listOf(
            this.first until other.first,
            other.last + 1..this.last
        ).filter { it.isNotEmpty() }
    }
    val first = if (other.first > this.first) this.first else other.last + 1
    val last = if (other.last < this.last) this.last else other.first - 1
    return listOf(first..last)
}
