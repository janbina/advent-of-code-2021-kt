package solutions

import utils.pairs
import java.io.BufferedReader

class Day18(
    inputReader: BufferedReader,
) : Day<Int, Int>() {

    private val input: List<SnailfishNumber> by lazy {
        inputReader.transformLines { SnailfishNumber.fromString(it) }
    }

    override fun solvePart1(): Int {
        return input.reduce { a, b ->
            (a + b).reduced()
        }.magnitude()
    }

    override fun solvePart2(): Int {
        return input.pairs().maxOf { (a, b) ->
            (a + b).reduced().magnitude()
        }
    }

    private sealed class SnailfishNumber {

        var parent: Pair? = null
            protected set

        val depth: Int get() = pathToRoot().count()

        abstract fun magnitude(): Int

        fun copy(): SnailfishNumber {
            return when (this) {
                is Regular -> Regular(this.value)
                is Pair -> createPair(
                    left = this.left.copy(),
                    right = this.right.copy(),
                )
            }
        }

        operator fun plus(other: SnailfishNumber): SnailfishNumber {
            return createPair(
                left = this.copy(),
                right = other.copy(),
            )
        }

        fun reduced(): SnailfishNumber {
            val reduced = this.copy()

            while (reduced.explode() || reduced.split()) Unit

            return reduced
        }

        private fun explode(): Boolean {
            val toExplode = inOrder()
                .filterIsInstance<Pair>()
                .firstOrNull { it.depth >= 4 } ?: return false

            val left = toExplode.left as? Regular
                ?: error("Exploding pairs have to consist of two regular numbers.")
            val right = toExplode.right as? Regular
                ?: error("Exploding pairs have to consist of two regular numbers.")

            toExplode.prevRegular()?.apply {
                replaceWith(Regular(value + left.value))
            }

            toExplode.nextRegular()?.apply {
                replaceWith(Regular(value + right.value))
            }

            toExplode.replaceWith(Regular(0))

            return true
        }

        private fun split(): Boolean {
            val toSplit = inOrder()
                .filterIsInstance<Regular>()
                .firstOrNull { it.value > 9 } ?: return false

            toSplit.replaceWith(
                createPair(
                    left = Regular(toSplit.value / 2),
                    right = Regular((toSplit.value + 1) / 2),
                )
            )

            return true
        }

        protected fun replaceWith(new: SnailfishNumber) {
            val parent = parent ?: return
            if (parent.left == this) parent.left = new
            if (parent.right == this) parent.right = new
            new.parent = parent
            this.parent = null
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

        fun pathToRoot(): Sequence<SnailfishNumber> = generateSequence(parent) { it.parent }

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
                return createPair(
                    left = fromString(splits[0]),
                    right = fromString(splits[1]),
                )
            }

            fun createPair(
                left: SnailfishNumber,
                right: SnailfishNumber,
            ): Pair {
                return Pair(left, right).also {
                    left.parent = it
                    right.parent = it
                }
            }

            private fun String.splitOnFreeComma(): List<String> {
                var bracketCount = 0
                var prevSplit = -1
                val list = mutableListOf<String>()

                forEachIndexed { index, c ->
                    when {
                        c == '[' -> bracketCount += 1
                        c == ']' -> bracketCount -= 1
                        c == ',' && bracketCount == 0 -> {
                            list.add(substring(prevSplit + 1, index))
                            prevSplit = index
                        }
                    }
                }

                list.add(substring(prevSplit + 1))

                return list
            }
        }
    }
}
