package solutions

import utils.getCyclic
import utils.setCyclic
import java.io.BufferedReader

class Day25(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: Array<Array<Char>> by lazy {
        inputReader.transformLines { line ->
            Array(line.length) { line[it] }
        }.toTypedArray()
    }

    override fun solvePart1(): Int {
        var steps = 0
        var cont = true
        var data = input

        while (cont) {
            cont = false
            steps += 1
            val newData = Array(data.size) { Array(data.first().size) { '.' } }

            for (y in data.indices) {
                for (x in data.first().indices) {
                    if (data[y][x] == '>') {
                        if (data.getCyclic(y, x + 1) == '.') {
                            newData.setCyclic(y, x + 1, '>')
                            cont = true
                        } else {
                            newData[y][x] = '>'
                        }
                    }
                }
            }

            for (x in data.first().indices) {
                for (y in data.indices) {
                    if (data[y][x] == 'v') {
                        if (data.getCyclic(y + 1, x) != 'v' && newData.getCyclic(y + 1, x) == '.') {
                            newData.setCyclic(y + 1, x, 'v')
                            cont = true
                        } else {
                            newData[y][x] = 'v'
                        }
                    }
                }
            }

            data = newData
        }

        return steps
    }

    override fun solvePart2(): Int {
        return 2
    }
}
