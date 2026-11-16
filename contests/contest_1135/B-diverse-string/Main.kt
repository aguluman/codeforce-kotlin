// Codeforces 1135 — Problem B: Diverse-String

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

fun solve() {
    val n = In.int()
    val k = In.int()
    val s = In.word()

    val ones = s.count { it == '1' }
    val zeros = n - ones
    val toggles = minOf(k, minOf(ones, zeros))
    val finalOnes: Int
    val finalZeros: Int

    if (ones >= zeros) {
        finalOnes = ones + toggles
        finalZeros = zeros - toggles
    } else {
        finalOnes = ones - toggles
        finalZeros = zeros + toggles
    }

    fun triangular(count: Int): Long = count.toLong() * (count + 1) / 2

    out.append(1L + triangular(finalOnes) + triangular(finalZeros)).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
