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

/** next[[i]]: the first position at or after i that holds [c], or s.length when there is none. */
fun nextPositions(s: String, c: Char): IntArray {
    val next = IntArray(s.length + 1) { s.length }

    for (i in s.length - 1 downTo 0) next[i] =
        if (s[i] == c) i else next[i + 1]

    return next
}

/**
 * For each index i of [s], how many good substrings contain it.
 *
 * Why this is correct:
 * - 2025 and 2026 both start with 202. From a start l, match 2, 0, 2 each at its earliest
 *   position, ending at `end`. Any 2025 or 2026 inside s[(l..r)] can be moved onto this earliest
 *   202, so s[(l..r)] contains 2025 exactly when a 5 sits in (end, r], and 2026 when 6 does.
 * - With first5 and first6 the first 5 and first 6 after `end`, s[(l..r)] is bad exactly when
 *   first5 <= r < first6. So for each start l, the bad ends form one range [first5, first6 - 1].
 * - Index i lies in (i + 1) * (n - i) substrings; the answer subtracts the bad ones among them.
 *   The bad range [a, b] of start l holds b - a + 1 substrings containing i when l <= i <= a,
 *   b - i + 1 when a < i <= b, and none after b. A constant, then a line in i: two difference
 *   arrays add each range in O(1), and one sweep reads every answer. O(n) in total.
 */
fun goodSubstringCounts(s: String): LongArray {
    val n = s.length
    val none = n   // nextPositions' "no such position"
    val next2 = nextPositions(s, '2')
    val next0 = nextPositions(s, '0')
    val next5 = nextPositions(s, '5')
    val next6 = nextPositions(s, '6')

    fun firstAfter(next: IntArray, position: Int): Int =
        if (position == none) none else next[position + 1]

    // Bad substrings containing i = flat(i) + slope(i) * i, built from range updates.
    val flat = LongArray(n + 1)
    val slope = LongArray(n + 1)

    fun addOver(from: Int, to: Int, constant: Long, perIndex: Long) {
        if (from > to) return
        flat[from] += constant; flat[to + 1] -= constant
        slope[from] += perIndex; slope[to + 1] -= perIndex
    }

    for (l in 0 until n) {
        val end = firstAfter(next2, firstAfter(next0, next2[l]))   // the earliest 202 from l
        if (end == none) continue                                   // no 202, so no 2025: all good
        val first5 = next5[end + 1]
        val lastBad = next6[end + 1] - 1                            // ends before the first 6
        if (first5 > lastBad) continue                              // no 5 before the 6: all good
        addOver(l, first5, (lastBad - first5 + 1).toLong(), 0)
        addOver(first5 + 1, lastBad, (lastBad + 1).toLong(), -1)
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