// Codeforces 2276 — Problem G: Elimination Of Players

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

const val MOD = 998_244_353L
const val MAX_N = 5000

val fact = LongArray(MAX_N + 1).also { f ->
    f[0] = 1
    for (i in 1..MAX_N) f[i] =
        f[i - 1] * i % MOD
}

fun eliminationXor(n: Int, a: IntArray): Long {
    val atPosition = fact[n - 1]
    val weakest = LongArray(n + 1) { atPosition }
    val runLength = IntArray(n + 1)
    val lastRunOf = IntArray(n + 1)
    val guardedUpTo = LongArray(n + 1)

    var runs = 0
    var oldestDistinctRun = 1
    var result = 0L

    for (round in 1 until n) {
        val x = a[round]

        if (round == 1 || x != a[round - 1]) {
            runs++
            runLength[runs] = 1
            oldestDistinctRun = maxOf(oldestDistinctRun, lastRunOf[x] + 1)
            lastRunOf[x] = runs
        }
        else {
            runLength[runs]++
        }

        val distinctRuns = runs - oldestDistinctRun + 1

        for (k in 1..distinctRuns) {
            val term = runLength[runs - k + 1] * fact[n - k - 1] % MOD
            guardedUpTo[k] = if (k % 2 == 1) (guardedUpTo[k - 1] + term) % MOD
            else (guardedUpTo[k - 1] - term + MOD) % MOD
        }

        for (j in 1..n) {
            if (j == x) {
                weakest[j] = (weakest[j] + atPosition) % MOD
                continue
            }
            val guarded = guardedUpTo[minOf(distinctRuns, runs - lastRunOf[j])]
            val eliminated = (weakest[j] + guarded) % MOD
            result = result xor (eliminated * j * round)
            weakest[j] = (atPosition - guarded + MOD) % MOD
        }
    }
    return result
}

fun solve() {
    val n = In.int()
    val a = IntArray(n)

    for (round in 1 until n) a[round] = In.int()

    out.append(eliminationXor(n, a)).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}