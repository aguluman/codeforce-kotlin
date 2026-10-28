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

fun nextPositions(s: String, c: Char): IntArray {
    val next = IntArray(s.length + 1) { s.length }
    for (i in s.length - 1 downTo 0) next[i] =
        if (s[i] == c) i else next[i + 1]

    return next
}

fun goodSubstringCounts(s: String): LongArray {
    val n = s.length
    val none = n
    val next2 = nextPositions(s, '2')
    val next0 = nextPositions(s, '0')
    val next5 = nextPositions(s, '5')
    val next6 = nextPositions(s, '6')

    fun firstAfter(next: IntArray, position: Int): Int =
        if (position == none) none else next[position + 1]

    val flat = LongArray(n + 1)
    val slope = LongArray(n + 1)

    fun addOver(from: Int, to: Int, constant: Long, perIndex: Long) {
        if (from > to) return
        flat[from] += constant; flat[to + 1] -= constant
        slope[from] += perIndex; slope[to + 1] -= perIndex
    }

    for (l in 0 until n) {
        val end = firstAfter(next2, firstAfter(next0, next2[l]))
        if (end == none) continue
        val first5 = next5[end + 1]
        val lastBad = next6[end + 1] - 1
        if (first5 > lastBad) continue
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
    In.int()
    val s = In.word()
    for (count in goodSubstringCounts(s))
        out.append(count).append(' ')

    out.append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
