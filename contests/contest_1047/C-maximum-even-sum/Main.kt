// Codeforces 1047 — Problem C: Maximum Even Sum

object In {
    private val stream = System.`in`
    private val buf = ByteArray(1 shl 16)
    private var len = 0
    private var pos = 0

    // Next byte, or -1 at end of input.
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

    // A whitespace-delimited token, e.g. a string or a grid row.
    fun word(): String {
        val sb = StringBuilder()
        var c = skipBlanks()
        while (c > ' '.code) {
            sb.append(c.toChar())
            c = read()
        }
        return sb.toString()
    }

    fun ints(n: Int) = IntArray(n) { int() }
    fun longs(n: Int) = LongArray(n) { long() }
}

val out = StringBuilder()

// a and b are up to 1e9, so a * b needs Long.
// Kotlin evaluates left to right, so reading a then b is safe in any form.
fun solve() {
    val a = In.long()
    val b = In.long()
    val answer = when {
        b % 2 == 1L -> if (a % 2 == 0L) -1 else a * b + 1  // b odd
        a % 2 == 1L && b % 4 != 0L -> -1                    // a odd, b = 2 mod 4
        else -> a * (b / 2) + 2                             // k = b / 2
    }
    out.append(answer).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
