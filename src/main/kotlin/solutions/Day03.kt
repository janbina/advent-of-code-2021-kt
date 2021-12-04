package solutions

import java.io.BufferedReader

class Day03(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: List<String> by lazy {
        inputReader.transformLines { it }
    }

    override fun solvePart1(): Int {
        val numLen = input.firstOrNull()?.length ?: 0

        val gamma = CharArray(numLen) { index ->
            val ones = input.count { it[index] == '1' }
            if (2 * ones > input.size) '1' else '0'
        }.concatToString()

        val epsilon = CharArray(gamma.length) { index ->
            if (gamma[index] == '1') '0' else '1'
        }.concatToString()

        return gamma.toInt(2) * epsilon.toInt(2)
    }

    override fun solvePart2(): Int {
        val oxRating = input.findByBitCriteria(true)
        val co2Rating = input.findByBitCriteria(false)

        return oxRating.toInt(2) * co2Rating.toInt(2)
    }

    private fun List<String>.findByBitCriteria(mostCommon: Boolean): String {
        var res = ""
        val sequence = asSequence()
        val numLen = firstOrNull()?.length ?: 0

        while (res.length < numLen) {
            val filtered = sequence.filter { it.startsWith(res) }
            val filteredCount = filtered.count()
            if (filteredCount == 1) return filtered.first()

            val oneCount = filtered.filter { it[res.length] == '1' }.count()

            res += when {
                mostCommon && 2 * oneCount >= filteredCount -> '1'
                mostCommon -> '0'
                2 * oneCount >= filteredCount -> '0'
                else -> '1'
            }
        }

        return res
    }
}
