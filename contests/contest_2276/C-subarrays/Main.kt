// Codeforces 2276 — Problem C: Subarrays

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

fun solve() {
    val n = In.int()
    val k = In.int()
    val a = In.ints(n)

    val prefix = IntArray(n + 1)
    for (i in 0 until n)
        prefix[i + 1] = (prefix[i] + a[i]) % k

    val seen = IntArray(k)
    var count = 0L
    for (j in 0..n) {
        val newlyAdded = j - k - 1
        if (newlyAdded >= 0)
            seen[prefix[newlyAdded]]++
        count += seen[prefix[j]]
    }

    out.append(count).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
