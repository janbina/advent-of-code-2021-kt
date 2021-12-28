package solutions

import utils.max
import utils.min
import java.io.BufferedReader

class Day24(
    inputReader: BufferedReader,
) : Day<Long, Long>() {

    private val instructions: List<Instruction> by lazy {
        inputReader.transformLines { Instruction.fromString(it) }
    }

    private val splittedInstructions: List<List<Instruction>> by lazy {
        val res = mutableListOf<MutableList<Instruction>>()

        instructions.forEach { instruction ->
            if (instruction is Instruction.Input) {
                res.add(mutableListOf(instruction))
            } else {
                res.last().add(instruction)
            }
        }

        res
    }

    private fun Long.digitList(): List<Int> = "$this".map { it.digitToInt() }

    override fun solvePart1(): Long {
        var poss = mapOf(mapOf('x' to 0) to 0L)
        repeat(14) { i ->
            val newPoss = mutableMapOf<Map<Char, Int>, Long>()

            poss.forEach { (prevVars, prevBiggestNum) ->
                for (input in 9 downTo 1) {
                    runCatching {
                        processInstructions(
                            splittedInstructions[i],
                            listOf(input),
                            prevVars,
                        )
                    }.onSuccess { newPoss[it] = prevBiggestNum * 10 + input }
                }
            }

            poss = newPoss
            println("Possibilities after $i = ${poss.size}")
        }

        val z0 = poss.filterKeys { it['z'] == 0 }.values
        println("Possibilities with z = 0: ${z0.size}")
        println(z0.max())
        println(z0.min())
        return 1
    }

    override fun solvePart2(): Long {
        return 2
    }

    private fun processInstructions(
        instructions: List<Instruction>,
        input: List<Int>,
        initial: Map<Char, Int> = mapOf(),
    ): Map<Char, Int> {
        val variables = initial.toMutableMap().withDefault { 0 }
        var inputIndex = 0

        instructions.forEachIndexed { index, instruction ->
            when (instruction) {
                is Instruction.Input -> {
                    variables[instruction.a.name] = input.getOrNull(inputIndex++)
                        ?: error("Not enough input data")
                }
                is Instruction.Add -> {
                    val a = instruction.a.name
                    val b = instruction.b.getValue(variables)
                    variables[a] = variables.getValue(a) + b
                }
                is Instruction.Div -> {
                    val a = instruction.a.name
                    val b = instruction.b.getValue(variables)
                    if (b == 0) error("Division by 0 - $instruction, $index")
                    variables[a] = variables.getValue(a) / b
                }
                is Instruction.Eql -> {
                    val a = instruction.a.name
                    val b = instruction.b.getValue(variables)
                    variables[a] = if (variables.getValue(a) == b) 1 else 0
                }
                is Instruction.Mod -> {
                    val a = instruction.a.name
                    val b = instruction.b.getValue(variables)
                    val aVal = variables.getValue(a)
                    if (aVal < 0) error("Mod with a < 0: $aVal - $instruction, $index")
                    if (b <= 0) error("Mod with b <= 0: $b - $instruction, $index")
                    variables[a] = variables.getValue(a) % b
                }
                is Instruction.Mul -> {
                    val a = instruction.a.name
                    val b = instruction.b.getValue(variables)
                    variables[a] = variables.getValue(a) * b
                }
            }
        }

        return variables.toMap()
    }

    private sealed class Op {
        data class Ref(val name: Char) : Op()
        data class Value(val value: Int) : Op()

        fun getValue(variables: Map<Char, Int>, default: Int = 0): Int = when (this) {
            is Ref -> variables[this.name] ?: default
            is Value -> this.value
        }
    }

    private sealed class Instruction {
        data class Input(val a: Op.Ref) : Instruction()
        data class Add(val a: Op.Ref, val b: Op) : Instruction()
        data class Mul(val a: Op.Ref, val b: Op) : Instruction()
        data class Div(val a: Op.Ref, val b: Op) : Instruction()
        data class Mod(val a: Op.Ref, val b: Op) : Instruction()
        data class Eql(val a: Op.Ref, val b: Op) : Instruction()

        companion object {
            fun fromString(string: String): Instruction {
                val splits = string.split(" ")
                if (splits.first() == "inp") {
                    require(splits.size == 2)
                    val c = splits[1]
                    require(c.length == 1)
                    return Input(Op.Ref(c.first()))
                }
                require(splits.size == 3)
                require(splits[1].length == 1)
                val a = Op.Ref(splits[1].first())
                val b = splits[2].toIntOrNull()?.let { Op.Value(it) } ?: Op.Ref(splits[2].first())
                return when (splits[0]) {
                    "add" -> Add(a, b)
                    "mul" -> Mul(a, b)
                    "div" -> Div(a, b)
                    "mod" -> Mod(a, b)
                    "eql" -> Eql(a, b)
                    else -> error("Invalid instruction \"$string\"")
                }
            }
        }
    }
}
