package solutions

import utils.productOf
import java.io.BufferedReader

class Day16(
    inputReader: BufferedReader,
) : Day<Int, Long>() {

    private val bitString: String by lazy {
        inputReader
            .useLines { it.first() }
            .map { char ->
                char.digitToInt(16).toString(2).padStart(4, '0')
            }.joinToString("")
    }

    override fun solvePart1(): Int {
        fun sumVersions(packets: List<Packet>): Int {
            return packets.sumOf { packet ->
                when (packet) {
                    is Packet.Literal -> packet.version
                    is Packet.Operator -> packet.version + sumVersions(packet.subPackets)
                }
            }
        }

        val parseResult = bitString.parseSinglePacket()
        return sumVersions(listOf(parseResult.packet))
    }

    override fun solvePart2(): Long {
        val parseResult = bitString.parseSinglePacket()
        return parseResult.packet.evaluate()
    }

    private fun String.parseSinglePacket(): ParseResult.Single {
        val version = this.take(3).toInt(2)
        val typeId = this.drop(3).take(3).toInt(2)
        val remainder = this.drop(6)

        return if (typeId == 4) {
            val content = remainder.parseLiteralContent()
            ParseResult.Single(
                packet = Packet.Literal(
                    version = version,
                    value = content.value,
                ),
                parsedLength = 6 + content.parsedLength,
            )
        } else {
            val content = remainder.parseOperatorContent()
            ParseResult.Single(
                packet = Packet.Operator(
                    version = version,
                    typeId = typeId,
                    subPackets = content.packets,
                ),
                parsedLength = 6 + content.parsedLength,
            )
        }
    }

    private fun String.parseOperatorContent(): ParseResult.Multiple {
        val lengthType = first()
        val length: Int
        var parsedLength = 1

        when (lengthType) {
            '0' -> {
                length = drop(1).take(15).toInt(2)
                parsedLength += 15
            }
            '1' -> {
                length = drop(1).take(11).toInt(2)
                parsedLength += 11
            }
            else -> error("Invalid char $lengthType")
        }

        var remainder = drop(parsedLength)
        val packets = mutableListOf<Packet>()

        if (lengthType == '0') {
            var subLength = 0
            while (subLength < length) {
                val result = remainder.parseSinglePacket()
                packets.add(result.packet)
                subLength += result.parsedLength
                remainder = remainder.drop(result.parsedLength)
            }
            require(subLength == length)
            parsedLength += subLength
        }

        if (lengthType == '1') {
            var subLength = 0
            repeat(length) {
                val result = remainder.parseSinglePacket()
                packets.add(result.packet)
                subLength += result.parsedLength
                remainder = remainder.drop(result.parsedLength)
            }
            parsedLength += subLength
        }

        return ParseResult.Multiple(
            packets = packets,
            parsedLength = parsedLength,
        )
    }

    private fun String.parseLiteralContent(): ParseResult.Literal {
        val chunks = mutableListOf<String>()
        for (i in 0..Int.MAX_VALUE) {
            val chunk = this.drop(i * 5).take(5)
            chunks.add(chunk)
            if (chunk.first() == '0') break
        }
        return ParseResult.Literal(
            value = chunks.joinToString(separator = "") { it.drop(1) }.toLong(2),
            parsedLength = chunks.sumOf { it.length },
        )
    }

    private fun printPackets(packets: List<Packet>, indent: Int = 0) {
        val indentString = (0 until indent).joinToString("") { " " }
        packets.forEach { packet ->
            when (packet) {
                is Packet.Literal -> {
                    println("${indentString}Literal: v=${packet.version}, value=${packet.value}")
                }
                is Packet.Operator -> {
                    println("${indentString}Operator: v=${packet.version}, t=${packet.typeId}")
                    printPackets(packet.subPackets, indent + 2)
                }
            }
        }
    }

    private sealed class ParseResult {
        abstract val parsedLength: Int

        class Single(
            val packet: Packet,
            override val parsedLength: Int,
        ): ParseResult()

        class Multiple(
            val packets: List<Packet>,
            override val parsedLength: Int,
        ): ParseResult()

        class Literal(
            val value: Long,
            override val parsedLength: Int,
        ): ParseResult()
    }

    private sealed class Packet {
        abstract val version: Int
        abstract val typeId: Int

        abstract fun evaluate(): Long

        class Literal(
            override val version: Int,
            val value: Long,
        ) : Packet() {
            override val typeId = 4

            override fun evaluate(): Long {
                return value
            }
        }

        class Operator(
            override val version: Int,
            override val typeId: Int,
            val subPackets: List<Packet>,
        ) : Packet() {

            override fun evaluate(): Long {
                return when (typeId) {
                    0 -> subPackets.sumOf { it.evaluate() }
                    1 -> subPackets.productOf { it.evaluate() }
                    2 -> subPackets.minOf { it.evaluate() }
                    3 -> subPackets.maxOf { it.evaluate() }
                    5 -> {
                        require(subPackets.size == 2)
                        if (subPackets.first().evaluate() > subPackets.last().evaluate()) 1 else 0
                    }
                    6 -> {
                        require(subPackets.size == 2)
                        if (subPackets.first().evaluate() < subPackets.last().evaluate()) 1 else 0
                    }
                    7 -> {
                        require(subPackets.size == 2)
                        if (subPackets.first().evaluate() == subPackets.last().evaluate()) 1 else 0
                    }
                    else -> error("Invalid type id: $typeId")
                }
            }
        }
    }
}
