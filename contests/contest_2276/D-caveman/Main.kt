// Codeforces 2276 — Problem D: Caveman

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
}

val out = StringBuilder()

enum class Verdict(val text: String) {
    BrainMelt("Brain Melt"),
    Two("2"),
    Many("Many")
}

fun classify(n: Int, m: Int, degree: IntArray): Verdict {
    val hasLonelyVertex =
        degree.any { it == 0 }
    val isStar =
        m == n - 1 && degree.any { it == n - 1 }

    return when {
        hasLonelyVertex -> Verdict.BrainMelt
        isStar          -> Verdict.Two
        else            -> Verdict.Many
    }
}

fun solve() {
    val n = In.int()
    val m = In.int()
    val degree = IntArray(n)

    repeat(m) {
        degree[In.int() - 1]++
        degree[In.int() - 1]++
    }

    out.append(classify(n, m, degree).text).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
