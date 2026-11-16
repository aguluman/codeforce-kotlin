// Codeforces 2276 — Problem E: Good Substrings

object In {
    private val stream = System.`in`
    private val buf = ByteArray(1 shl 16)
    private var len = 0
    private var pos = 0

    private fun read(): Int {
        if (pos == len) {
            len = stream.read(buf, 0, buf.size)
            pos = 0
            if (len <= 0) {
                len = 0
                return -1
            }
        }
        return buf[pos++].toInt()
    }

    private fun skipBlanks(): Int {
        var c = read()
        while (c != -1 && c <= ' '.code) c = read()
        return c
    }

    fun long(): Long {
        var c = skipBlanks()
        val negative = c == '-'.code
        if (negative) c = read()
        var r = 0L
        while (c >= '0'.code && c <= '9'.code) {
            r = r * 10 + (c - '0'.code)
            c = read()
        }
        return if (negative) -r else r
    }

    fun int(): Int = long().toInt()

    fun word(): String {
        val sb = StringBuilder()
        var c = skipBlanks()
        while (c > ' '.code) {
            sb.append(c.toChar())
            c = read()
        }
        return sb.toString()
    }
}

val out = StringBuilder()

/** A position in the string, or NONE; asking NONE for its index fails loudly. */
@JvmInline
value class Position private constructor(private val raw: Int) {
    val exists: Boolean get() = raw >= 0
    val index: Int get() = raw.also { check(it >= 0) { "NONE has no index" } }
    fun indexOr(fallback: Int): Int = if (exists) raw else fallback

    companion object {
        val NONE = Position(-1)
        fun at(index: Int): Position = if (index >= 0) Position(index) else NONE
    }
}

/** Where one digit appears: the first position holding it at or after any index. */
class Occurrences(s: String, digit: Char) {
    private val next = IntArray(s.length + 1) { -1 }.also { next ->
        for (i in s.length - 1 downTo 0) next[i] = if (s[i] == digit) i else next[i + 1]
    }

    fun atOrAfter(i: Int): Position = Position.at(next[i])
    fun after(position: Position): Position = if (position.exists) atOrAfter(position.index + 1) else Position.NONE
}

/** For each index of [s], how many good substrings contain it: all substrings minus the bad ones. */
fun goodSubstringCounts(s: String): LongArray {
    val n = s.length
    val twos = Occurrences(s, '2')
    val zeros = Occurrences(s, '0')
    val fives = Occurrences(s, '5')
    val sixes = Occurrences(s, '6')
    val flat = LongArray(n + 1)
    val slope = LongArray(n + 1)

    fun addOver(from: Int, to: Int, constant: Long, perIndex: Long) {
        if (from > to) return
        flat[from] += constant; flat[to + 1] -= constant
        slope[from] += perIndex; slope[to + 1] -= perIndex
    }

    for (l in 0 until n) {
        val end = twos.after(zeros.after(twos.atOrAfter(l)))
        val first5 = fives.after(end)
        if (!first5.exists) continue
        val lastBad = sixes.after(end).indexOr(n) - 1
        if (first5.index > lastBad) continue
        addOver(l, first5.index, (lastBad - first5.index + 1).toLong(), 0)
        addOver(first5.index + 1, lastBad, (lastBad + 1).toLong(), -1)
    }

    val good = LongArray(n)
    var runningFlat = 0L
    var runningSlope = 0L

    for (i in 0 until n) {
        runningFlat += flat[i]
        runningSlope += slope[i]
        val containing = (i + 1).toLong() * (n - i)
        good[i] = containing - (runningFlat + runningSlope * i)
    }
    return good
}

fun solve() {
    In.int()             // n: the string carries its own length
    val s = In.word()
    for (count in goodSubstringCounts(s))
        out.append(count).append(' ')

    out.append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}