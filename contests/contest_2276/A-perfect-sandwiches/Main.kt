// Codeforces 2276 — Problem A: Perfect Sandwiches

// Byte-level stdin reader. On 2 million ints it took 54 ms, against 228 ms
// for BufferedReader + StringTokenizer and 931 ms for readLine() + split.
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

// Build all output here and print it once: println per line was about
// 5 times slower for 200,000 lines.
val out = StringBuilder()

fun solve() {
    val a = In.int()
    val b = In.int()

    val bySausage = b

    val byCheese = a - bySausage

    val answer = if (byCheese <= 0) -1 else minOf(bySausage, byCheese)

    out.append(answer).append('\n')
}

fun main() {
    // Single test case? Replace the loop with: solve()
    repeat(In.int()) { solve() }
    print(out)
}
