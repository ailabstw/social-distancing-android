package tw.gov.cdc.exposurenotifications.hcert.decode

import kotlin.math.pow
import kotlin.math.roundToLong

object Base45Encoder {

    // https://datatracker.ietf.org/doc/draft-faltstrom-base45/?include_text=1
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

    fun encode(input: ByteArray) =
        input.asSequence().map { it.toUByte() }
            .chunked(2).map(this::encodeTwoCharsPadded)
            .flatten().joinToString(separator = "")

    private fun encodeTwoCharsPadded(input: List<UByte>): List<Char> {
        val result = encodeTwoChars(input).toMutableList()
        when (input.size) {
            1 -> if (result.size < 2) result += '0'
            2 -> while (result.size < 3) result += '0'
        }
        return result
    }

    private fun encodeTwoChars(list: List<UByte>) =
        generateSequenceByDivRem(toTwoCharValue(list), 45)
            .map { ALPHABET[it] }.toList()

    private fun toTwoCharValue(list: List<UByte>) =
        list.reversed().foldIndexed(0L) { index, acc, element ->
            pow(256, index) * element.toShort() + acc
        }

    fun decode(input: String) =
        input.chunked(3).map(this::decodeThreeCharsPadded)
            .flatten().map { it.toByte() }.toByteArray()

    private fun decodeThreeCharsPadded(input: String): List<UByte> {
        val result = decodeThreeChars(input).toMutableList()
        when (input.length) {
            3 -> while (result.size < 2) result += 0U
            1 -> throw IllegalArgumentException() // per spec
        }
        return result.reversed()
    }

    private fun decodeThreeChars(list: String) =
        generateSequenceByDivRem(fromThreeCharValue(list), 256)
            .map { it.toUByte() }.toList()

    private fun fromThreeCharValue(list: String): Long {
        return list.foldIndexed(0L) { index, acc: Long, element ->
            if (!ALPHABET.contains(element)) throw IllegalArgumentException()
           val result = pow(45, index) * ALPHABET.indexOf(element) + acc
            if (list.length == 2 && result > 255) throw IllegalArgumentException() // per spec
            if (result > 65535) throw IllegalArgumentException() // per spec
            result
        }
    }

    private fun generateSequenceByDivRem(seed: Long, divisor: Int) =
        generateSequence(seed) { if (it >= divisor) it.div(divisor) else null }
            .map { it.rem(divisor).toInt() }

    private fun pow(base: Int, exp: Int) = base.toDouble().pow(exp.toDouble()).roundToLong()

}
