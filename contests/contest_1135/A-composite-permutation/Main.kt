// Codeforces 1135 — Problem A: Composite-Permutation

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

fun solve() {
    val n = In.int()
    if (n < 4) {
        out.append(-1).append('\n')
        return
    }

    for (i in 1..n) {
        if (i > 1) out.append(' ')
        val value = when (i) {
            1    -> 4
            4    -> 1
            else -> i
        }
        out.append(value)
    }
    out.append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
