// Codeforces 2276 — Problem B: Replace And Compress

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
    val s = In.word()
    val n = s.length

    var canSplitBlock = false    // some flip turns 000/111 into 010/101: +2 runs
    var canMergeBlocks = false   // some flip turns 010/101 into 000/111: −2 runs

    for (i in 1 until n - 1) {
        val left = s[i - 1]
        val mid = s[i]
        val right = s[i + 1]

        if (left == mid && mid == right) canSplitBlock = true
        if (left == right && mid != left) canMergeBlocks = true
    }

    val answer = 1 + (if (canSplitBlock) 1 else 0) + (if (canMergeBlocks) 1 else 0)

    out.append(answer).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
