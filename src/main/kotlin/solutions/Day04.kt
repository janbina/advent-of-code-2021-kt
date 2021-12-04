package solutions

import java.io.BufferedReader

class Day04(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: Input by lazy { inputReader.parseInput() }

    override fun solvePart1(): Int {
        return input.boards.map { it.play(input.numbers) }
            .sortedWith(compareBy({ it.turns }, { -it.score }))
            .first()
            .score
    }

    override fun solvePart2(): Int {
        return input.boards.map { it.play(input.numbers) }
            .sortedWith(compareBy({ it.turns }, { -it.score }))
            .last()
            .score
    }

    private fun BufferedReader.parseInput(): Input {
        val numbers = readLine().split(",").map { it.toInt() }
        val boards = mutableListOf<Board>()

        while (true) {
            val emptyLine = runCatching { readLine() }.getOrNull() ?: break
            require(emptyLine.isBlank())
            val board = parseBoard() ?: break
            boards.add(board)
        }

        val boardSize = boards.firstOrNull()?.size ?: 0
        boards.forEach {
            require(it.size == boardSize)
        }

        return Input(numbers, boards)
    }

    private fun BufferedReader.parseBoard(): Board? {
        val firstLine: String
        while (true) {
            val line = runCatching { readLine() }.getOrNull() ?: return null
            if (line.isNotBlank()) {
                firstLine = line
                break
            }
        }
        val firstRow = firstLine.toBoardRow()
        return Array(firstRow.size) { index ->
            if (index == 0) {
                firstRow
            } else {
                readLine().toBoardRow().also {
                    require(it.size == firstRow.size)
                }
            }
        }
    }

    private fun String.toBoardRow(): Array<Int> {
        return trim().split("\\s+".toRegex()).map { it.toInt() }.toTypedArray()
    }

    private fun Board.play(numbers: List<Int>): GameResult {
        (size..numbers.size).forEach { turns ->
            val score = getScore(numbers.take(turns))
            if (score > 0) return GameResult(turns, score)
        }
        return GameResult(Int.MAX_VALUE, 0)
    }

    private fun Board.getScore(numbersDrawn: List<Int>): Int {
        val drawnSet = numbersDrawn.toSet()

        if (!isWon(drawnSet)) return 0

        val notDrawnSum = flatMap { it.asIterable() }.filter { it !in drawnSet }.sum()

        return notDrawnSum * numbersDrawn.last()
    }

    private fun Board.isWon(numbersDrawn: Set<Int>): Boolean {
        forEach { row ->
            if (row.all { it in numbersDrawn }) {
                return true
            }
        }

        indices.forEach { colIndex ->
            val column = map { it[colIndex] }
            if (column.all { it in numbersDrawn }) {
                return true
            }
        }

        return false
    }

    private class Input(
        val numbers: List<Int>,
        val boards: List<Board>,
    )

    private class GameResult(
        val turns: Int,
        val score: Int,
    )
}

typealias Board = Array<Array<Int>>
