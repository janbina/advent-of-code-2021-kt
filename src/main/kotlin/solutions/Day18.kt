package solutions

import utils.pairs
import utils.uniquePairs
import java.io.BufferedReader

class Day18(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: List<SnailfishNumber> by lazy {
        inputReader.transformLines { SnailfishNumber.fromString(it) }
    }

    override fun solvePart1(): Int {
        val sum = input.reduce { a, b -> (a + b).reduced() }
        return sum.magnitude()
    }

    override fun solvePart2(): Int {
        return input.pairs().maxOf { (a, b) ->
            (a + b).reduced().magnitude()
        }
    }

    private sealed class SnailfishNumber {

        var parent: Pair? = null
            protected set

        val depth: Int
            get() {
                var d = 0
                var p = parent
                while (p != null) {
                    d++
                    p = p.parent
                }
                return d
            }

        fun copy(): SnailfishNumber {
            return when (this) {
                is Regular -> Regular(this.value)
                is Pair -> {
                    val left = this.left.copy()
                    val right = this.right.copy()
                    val top = Pair(left, right)
                    left.parent = top
                    right.parent = top
                    top
                }
            }
        }

        operator fun plus(other: SnailfishNumber): SnailfishNumber {
            val left = this.copy()
            val right = other.copy()
            val newTop = Pair(
                left = left,
                right = right,
            )
            left.parent = newTop
            right.parent = newTop
            return newTop
        }

        fun reduced(): SnailfishNumber {
            val reduced = this.copy()

            while (true) {
                val toExplode = reduced.firstToExplode()
                if (toExplode != null) {
                    val left = toExplode.left as? Regular
                        ?: error("Exploding pairs have to consist of two regular numbers.")
                    val right = toExplode.right as? Regular
                        ?: error("Exploding pairs have to consist of two regular numbers.")

                    toExplode.prevRegular()?.let { prevRegular ->
                        if (prevRegular.parent!!.left == prevRegular) {
                            prevRegular.parent!!.left = Regular(prevRegular.value + left.value).also { it.parent = prevRegular.parent }
                        }
                        if (prevRegular.parent!!.right == prevRegular) {
                            prevRegular.parent!!.right = Regular(prevRegular.value + left.value).also { it.parent = prevRegular.parent }
                        }
                    }

                    toExplode.nextRegular()?.let { nextRegular ->
                        if (nextRegular.parent!!.left == nextRegular) {
                            nextRegular.parent!!.left = Regular(nextRegular.value + right.value).also { it.parent = nextRegular.parent }
                        }
                        if (nextRegular.parent!!.right == nextRegular) {
                            nextRegular.parent!!.right = Regular(nextRegular.value + right.value).also { it.parent = nextRegular.parent }
                        }
                    }

                    if (toExplode.parent!!.left == toExplode) {
                        toExplode.parent!!.left = Regular(0).also { it.parent = toExplode.parent }
                    }
                    if (toExplode.parent!!.right == toExplode) {
                        toExplode.parent!!.right = Regular(0).also { it.parent = toExplode.parent }
                    }
                    continue
                }
                val toSplit = reduced.firstToSplit()
                if (toSplit != null) {
                    val newNode = Pair(
                        left = Regular(toSplit.value / 2),
                        right = Regular((toSplit.value + 1) / 2),
                    )
                    newNode.left.parent = newNode
                    newNode.right.parent = newNode
                    newNode.parent = toSplit.parent
                    if (toSplit.parent!!.left == toSplit) {
                        toSplit.parent!!.left = newNode
                    }
                    if (toSplit.parent!!.right == toSplit) {
                        toSplit.parent!!.right = newNode
                    }
                    continue
                }
                break
            }

            return reduced
        }

        fun inOrder(): Sequence<SnailfishNumber> = sequence {
            when (val current = this@SnailfishNumber) {
                is Pair -> {
                    yieldAll(current.left.inOrder())
                    yield(current)
                    yieldAll(current.right.inOrder())
                }
                is Regular -> yield(current)
            }
        }

        abstract fun magnitude(): Int

        private fun firstToExplode(): Pair? {
            return inOrder().filterIsInstance<Pair>().firstOrNull { it.depth >= 4 }
        }

        private fun firstToSplit(): Regular? {
            return inOrder().filterIsInstance<Regular>().firstOrNull { it.value > 9 }
        }

        protected fun prevRegular(): Regular? {
            var current: SnailfishNumber? = this
            while (current != null) {
                val parent = current.parent
                if (parent?.left != current) {
                    current = parent?.left
                    break
                }
                current = parent
            }

            while (current is Pair) {
                current = current.right
            }

            return current as? Regular
        }

        protected fun nextRegular(): Regular? {
            var current: SnailfishNumber? = this
            while (current != null) {
                val parent = current.parent
                if (parent?.right != current) {
                    current = parent?.right
                    break
                }
                current = parent
            }

            while (current is Pair) {
                current = current.left
            }

            return current as? Regular
        }

        class Pair(
            left: SnailfishNumber,
            right: SnailfishNumber,
        ) : SnailfishNumber() {

            var left: SnailfishNumber = left
                internal set

            var right: SnailfishNumber = right
                internal set

            override fun toString(): String {
                return "[$left,$right]"
            }

            override fun magnitude(): Int {
                return 3 * left.magnitude() + 2 * right.magnitude()
            }
        }

        class Regular(
            val value: Int,
        ) : SnailfishNumber() {
            override fun toString(): String {
                return "$value"
            }

            override fun magnitude(): Int = value
        }

        companion object {

            fun fromString(string: String): SnailfishNumber {
                val intValue = string.toIntOrNull()
                if (intValue != null) return Regular(intValue)
                require(string.first() == '[')
                require(string.last() == ']')
                val splits = string.drop(1).dropLast(1).splitOnFreeComma()
                require(splits.size == 2)
                val left = fromString(splits[0])
                val right = fromString(splits[1])
                val top = Pair(
                    left = left,
                    right = right,
                )
                left.parent = top
                right.parent = top
                return top
            }

            private fun String.splitOnFreeComma(): List<String> {
                var bracketCount = 0

                forEachIndexed { index, c ->
                    when (c) {
                        '[' -> bracketCount += 1
                        ']' -> bracketCount -= 1
                        ',' -> {
                            if (bracketCount == 0) {
                                return listOf(
                                    substring(
                                        0,
                                        index
                                    )
                                ) + substring(index + 1).splitOnFreeComma()
                            }
                        }
                        else -> Unit
                    }
                }

                return listOf(this)
            }
        }
    }
}
