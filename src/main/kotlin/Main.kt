import solutions.*
import java.io.BufferedReader
import java.io.File

fun main() {
    runDay(6)
    testAll()
}

private fun runDay(
    dayNumber: Int,
    input: BufferedReader? = null,
) {
    val inputReader = input
        ?: getDayInputFile(dayNumber)?.bufferedReader()
        ?: error("No input for day $dayNumber")
    val day = createDay(dayNumber, inputReader)
    println("Solving Day $dayNumber")
    println("\tPart 1 = ${day.solvePart1()}")
    println("\tPart 2 = ${day.solvePart2()}")
}

private fun testAll() {
    Day01(getDayInputFile(1)!!.bufferedReader()).run {
        require(solvePart1() == 1387)
        require(solvePart2() == 1362)
    }
    Day02(getDayInputFile(2)!!.bufferedReader()).run {
        require(solvePart1() == 1692075)
        require(solvePart2() == 1749524700)
    }
    Day03(getDayInputFile(3)!!.bufferedReader()).run {
        require(solvePart1() == 3912944)
        require(solvePart2() == 4996233)
    }
    Day04(getDayInputFile(4)!!.bufferedReader()).run {
        require(solvePart1() == 2496)
        require(solvePart2() == 25925)
    }
    Day05(getDayInputFile(5)!!.bufferedReader()).run {
        require(solvePart1() == 5632)
        require(solvePart2() == 22213)
    }
    Day06(getDayInputFile(6)!!.bufferedReader()).run {
        require(solvePart1() == 359999L)
        require(solvePart2() == 1631647919273L)
    }
}

private fun getDayInputFile(day: Int): File? {
    val fileName = "day${day.toString().padStart(2, '0')}.txt"
    val fileUri = Day::class.java.classLoader.getResource(fileName)?.toURI() ?: return null
    return File(fileUri)
}

private fun createDay(day: Int, input: BufferedReader): Day<*, *> {
    return when (day) {
        1 -> Day01(input)
        2 -> Day02(input)
        3 -> Day03(input)
        4 -> Day04(input)
        5 -> Day05(input)
        6 -> Day06(input)
        else -> error("Day $day not yet implemented")
    }
}
