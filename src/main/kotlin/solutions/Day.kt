package solutions

import java.io.BufferedReader

abstract class Day<P1, P2> {

    abstract fun solvePart1(): P1

    abstract fun solvePart2(): P2

    inline fun <reified T> BufferedReader.transformLines(
        noinline transform: (String) -> T,
    ): List<T> = useLines { lines ->
        lines.map(transform).toList()
    }
}
