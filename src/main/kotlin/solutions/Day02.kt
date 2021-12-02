package solutions

import java.io.BufferedReader

class Day02(
    inputReader: BufferedReader,
): Day<Int, Int>() {

    private val input: List<Instruction> by lazy {
        inputReader.transformLines { line ->
            val (direction, amount) = line.split(" ")
            val amountInt = amount.toInt()
            when (direction) {
                "up" -> Instruction.Up(amountInt)
                "down" -> Instruction.Down(amountInt)
                "forward" -> Instruction.Forward(amountInt)
                else -> error("Invalid instruction")
            }
        }
    }

    override fun solvePart1(): Int {
        var x = 0
        var depth = 0
        input.forEach { instruction ->
            when (instruction) {
                is Instruction.Up -> depth -= instruction.amount
                is Instruction.Down -> depth += instruction.amount
                is Instruction.Forward -> x += instruction.amount
            }
        }
        return x * depth
    }

    override fun solvePart2(): Int {
        var x = 0
        var depth = 0
        var aim = 0
        input.forEach { instruction ->
            when (instruction) {
                is Instruction.Up -> aim -= instruction.amount
                is Instruction.Down -> aim += instruction.amount
                is Instruction.Forward -> {
                    x += instruction.amount
                    depth += instruction.amount * aim
                }
            }
        }
        return x * depth
    }

    private sealed class Instruction {
        abstract val amount: Int

        class Up(override val amount: Int): Instruction()
        class Down(override val amount: Int): Instruction()
        class Forward(override val amount: Int): Instruction()
    }
}
