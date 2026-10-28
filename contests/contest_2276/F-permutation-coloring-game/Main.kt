// Codeforces 2276 — Problem F: Permutation Colouring Game

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

enum class Winner {
    Alice,
    Bob
}

fun cycleLengthCounts(p: IntArray): IntArray {
    val n = p.size
    val counts = IntArray(n + 1)
    val seen = BooleanArray(n)
    for (start in 0 until n) {
        if (seen[start]) continue
        var length = 0
        var i = start
        while (!seen[i]) {
            seen[i] = true
            i = p[i] - 1
            length++
        }
        counts[length]++
    }
    return counts
}


fun winner(counts: IntArray, k: Int): Winner {
    val long = k + 1
    if ((long + 1 until counts.size).any { counts[it] > 0 }) return Winner.Bob
    val left = IntArray(long + 1) { s -> if (s < counts.size) counts[s] else 0 }
    val singles = left[1]

    fun threat(): Int {
        var slots = k
        var total = 0
        for (s in long downTo 2) {
            val take = minOf(left[s], slots)
            total += take * (s - 1)
            slots -= take
        }
        return total
    }

    fun singlesComplete(spent: Int): Boolean {
        val fewest = if (spent == 0) 1 else 0
        val most = minOf(singles, k - spent)
        if (fewest > most || threat() > k) return false
        if (left[long] == 0) return true
        val r = fewest + ((singles - fewest) % long + long) % long
        return r <= most
    }

    fun firstMoveWins(size: Int, spent: Int): Boolean {
        if (size == 1) return singlesComplete(spent)
        for (take in 0..minOf(left[size], (k - spent) / size)) {
            left[size] -= take
            val wins = firstMoveWins(size - 1, spent + take * size)
            left[size] += take
            if (wins) return true
        }
        return false
    }

    return if (firstMoveWins(k, 0)) Winner.Alice else Winner.Bob
}

fun solve() {
    val n = In.int()
    val k = In.int()
    val p = In.ints(n)
    out.append(winner(cycleLengthCounts(p), k).name).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}