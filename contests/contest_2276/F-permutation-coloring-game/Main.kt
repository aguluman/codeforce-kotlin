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

/** The two possible outcomes; their names are printed as they are. */
enum class Winner { Alice, Bob }

/** counts[[s]] = how many cycles of length s the permutation [p] has. */
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

/**
 * Who wins, from the permutation's cycle lengths alone. Why this is correct:
 *
 * 1. A permutation sorts by same-colour swaps exactly when every cycle is one colour.
 * 2. Alice must never leave a cycle part-coloured: Bob would paint one of its white elements the
 *    other colour. So she colours only whole cycles, plus whatever Bob started.
 * 3. Bob's threat: colour one element in each of up to k cycles. Alice must finish all of them in
 *    one turn, which needs the sum of (length - 1) over them to be at most k. The biggest such sum
 *    comes from the k longest cycles. Cycles only ever disappear, so the threat never grows: if it is
 *    at most k after Alice's first move, Bob can never win this way. A cycle longer than k + 1 makes
 *    the threat at least k + 1 for good, so Bob wins.
 * 4. A cycle of exactly k + 1 can't be coloured whole by Alice. If none remains after her first move,
 *    she always has a legal move, so she wins.
 * 5. If one remains with the threat at most k, every other cycle is a single element (for k = 1,
 *    several 2-cycles may remain, and Bob opening one is just a pass). Players take 1..k singletons;
 *    Alice loses if she faces none, and Bob facing none must open the long cycle for Alice to finish.
 *    Bob moves first, so he wins exactly when the singletons are not a multiple of k + 1.
 * 6. So Alice wins exactly when some first move (whole cycles, 1..k elements) leaves the threat at
 *    most k and, if a (k + 1)-cycle remains, a multiple of k + 1 singletons. The longer cycles she
 *    can remove total at most k <= 20, so there are at most 627 combinations to try.
 */
fun winner(counts: IntArray, k: Int): Winner {
    val long = k + 1
    if ((long + 1 until counts.size).any { counts[it] > 0 }) return Winner.Bob
    val left = IntArray(long + 1) { s -> if (s < counts.size) counts[s] else 0 }
    val singles = left[1]

    /** The most Bob can make Alice finish in one turn: one element from each of the k longest cycles. */
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

    /** Having coloured longer cycles worth [spent], can some number of singletons finish a winning first move? */
    fun singlesComplete(spent: Int): Boolean {
        val fewest = if (spent == 0) 1 else 0   // a move must colour at least one element
        val most = minOf(singles, k - spent)
        if (fewest > most || threat() > k) return false
        if (left[long] == 0) return true
        // The smallest r >= fewest that leaves a multiple of k + 1 singletons.
        val r = fewest + ((singles - fewest) % long + long) % long
        return r <= most
    }

    /** Try every way to colour whole cycles of length [size] down to 2 on the first move. */
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