package solutions

import java.io.BufferedReader

class Day21(
    inputReader: BufferedReader,
) : Day<Int, Long>() {

    private val inputLines = inputReader.readLines()
    private val player1start = inputLines.firstNotNullOf {
        it.substringAfter("Player 1 starting position: ").toIntOrNull()
    }
    private val player2start = inputLines.firstNotNullOf {
        it.substringAfter("Player 2 starting position: ").toIntOrNull()
    }

    private fun Int.moveOnBoardBy(value: Int): Int {
        return (this + value - 1) % 10 + 1
    }

    override fun solvePart1(): Int {
        val targetScore = 1000
        var diceRolls = 0

        fun getMoveForRound(): Int {
            return (1..3).sumOf {
                diceRolls += 1
                (diceRolls - 1) % 100 + 1
            }
        }

        var p1score = 0
        var p1position = player1start
        var p2score = 0
        var p2position = player2start

        while (true) {
            p1position = p1position.moveOnBoardBy(getMoveForRound())
            p1score += p1position
            if (p1score >= targetScore) break
            p2position = p2position.moveOnBoardBy(getMoveForRound())
            p2score += p2position
            if (p2score >= targetScore) break
        }

        return diceRolls * minOf(p1score, p2score)
    }

    override fun solvePart2(): Long {
        val roundMoves = getPossibleRoundMoves(1..3, 3)
        val player1Rounds = getScoreReachRoundNumbers(player1start, roundMoves, 21)
        val player2Rounds = getScoreReachRoundNumbers(player2start, roundMoves, 21)

        // for player one, each round we multiply number universes in which player 1 won that round
        // with player2's number of universes that were not yet decided the *round before*, because
        // once player1 wins, player2 does not play its round
        val player1Wins = player1Rounds.entries.sumOf { (round, state) ->
            state.universesWonInThisRound * (player2Rounds[round - 1]?.universesNotYetWon ?: 0)
        }
        // for player two, each round we multiply number universes in which player 2 won that round
        // with player1's number of universes that were not yet decided in that round
        val player2Wins = player2Rounds.entries.sumOf { (round, state) ->
            state.universesWonInThisRound * (player1Rounds[round]?.universesNotYetWon ?: 0)
        }

        return maxOf(player1Wins, player2Wins)
    }

    /**
     * For [diceRange] and [rollsCount] per round, creates map of possible move values
     * and their frequencies.
     * For example, for [diceRange] 1..2 and [rollsCount] 2, possible rolls and their sums are:
     * 1, 1 -> 2
     * 1, 2 -> 3
     * 2, 1 -> 3
     * 2, 2 -> 4
     * thus, resulting map would look like this:
     * [2 -> 1, 3 -> 2, 4 -> 1]
     */
    private fun getPossibleRoundMoves(
        diceRange: IntRange,
        rollsCount: Int,
    ): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()

        fun rollDice(
            rollNumber: Int = 1,
            sumBeforeRoll: Int = 0,
        ) {
            for (num in diceRange) {
                val sum = sumBeforeRoll + num
                if (rollNumber == rollsCount) {
                    map[sum] = (map[sum] ?: 0) + 1
                } else {
                    rollDice(rollNumber + 1, sum)
                }
            }
        }

        rollDice()

        return map
    }

    /**
     * Returns map from round number to [RoundState], which says how many universes were won
     * in that round and how many universes were not yet decided.
     */
    private fun getScoreReachRoundNumbers(
        startPosition: Int,
        roundMoves: Map<Int, Int>,
        targetScore: Int,
    ): Map<Int, RoundState> {
        val map = mutableMapOf<Int, RoundState>()

        fun playRound(
            round: Int = 1,
            positionBefore: Int = startPosition,
            scoreBefore: Int = 0,
            universesBefore: Long = 1,
        ) {
            roundMoves.forEach { (move, moveUniverses) ->
                val position = positionBefore.moveOnBoardBy(move)
                val score = scoreBefore + position
                val universes = universesBefore * moveUniverses

                if (score >= targetScore) {
                    // universes that won in this round
                    // add them to the state and don't expand them further
                    val cur = map[round] ?: RoundState()
                    map[round] = cur.copy(
                        universesWonInThisRound = cur.universesWonInThisRound + universes
                    )
                } else {
                    // universes that didn't yet reached won state
                    // add them to the state and plan for them next round
                    val cur = map[round] ?: RoundState()
                    map[round] = cur.copy(
                        universesNotYetWon = cur.universesNotYetWon + universes
                    )
                    playRound(
                        round = round + 1,
                        positionBefore = position,
                        scoreBefore = score,
                        universesBefore = universes,
                    )
                }
            }
        }

        playRound()

        return map
    }

    private data class RoundState(
        val universesWonInThisRound: Long = 0,
        val universesNotYetWon: Long = 0,
    )
}
