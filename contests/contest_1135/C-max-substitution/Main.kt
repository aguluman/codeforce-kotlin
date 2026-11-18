// Codeforces 1135 — Problem C: Max-Substitution

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

    fun ints(n: Int) = IntArray(n) { int() }
}

val out = StringBuilder()

const val MOD = 1_000_000_007L

fun modPow(base: Int, exponent: Int): Long {
    var result = 1L
    var factor = base.toLong()
    var power = exponent

    while (power > 0) {
        if ((power and 1) == 1) {
            result = result * factor % MOD
        }
        factor = factor * factor % MOD
        power = power shr 1
    }

    return result
}

fun solve() {
    val n = In.int()
    val a = In.ints(n)
    a.sort()

    var answer = 1L

    for (i in n - 1 downTo 0) {
        if (i > 0 && a[i] == a[i - 1]) break
        answer = (answer + modPow(a[i] - 1, n - i)) % MOD
    }

    out.append(answer).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
