// Codeforces 2276 — Problem G: Elimination of Players

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
}

// Build all output here and print it once: println per line was about
// 5 times slower for 200,000 lines.
val out = StringBuilder()

const val MOD = 998_244_353L
const val MAX_N = 5000

/** fact[[i]] = i! mod MOD. */
val fact = LongArray(MAX_N + 1).also { f ->
    f[0] = 1
    for (i in 1..MAX_N) f[i] = f[i - 1] * i % MOD
}

/**
 * XOR over all players j and rounds i of (ans(j, i) mod MOD) * j * i, for protected players a[1..n-1].
 *
 * Why this is correct:
 *
 * 1. Line the players up weakest first. Keep c, the weakest survivor. In round i, the player at
 *    position i + 1 arrives: if c is protected (c == a[[i]]) the arrival is eliminated, otherwise c is
 *    and the arrival becomes c. So for j != a[[i]]:
 *      ans(j, i) = weakest(j) + guarded(j),
 *    weakest(j) = permutations where c == j in round i,
 *    guarded(j) = permutations where j arrives in round i while c == a[[i]].
 * 2. Any fixed player sits at any fixed position in (n - 1)! permutations. So c becomes j after round i
 *    in (n - 1)! - guarded(j) permutations (j arrived, and c was not protected). And a[[i]] itself
 *    stays c if it already was, or arrives and takes over: weakest(a[[i]]) grows by (n - 1)!.
 * 3. c == x == a[[i]] in round i exactly when x arrived during the current run of equal a values and
 *    the round before its arrival was unprotected. Inside the run that is automatic. At the run's
 *    first position it fails exactly when the previous run's player was protecting the end of the
 *    previous run, which is the same question one run back. Inclusion-exclusion along the runs gives
 *      guarded(j) = sum over t = 1..k of (-1)^(t+1) * length(run t back) * (n - t - 1)!,
 *    since fixing t run players at t positions and j at position i + 1 leaves (n - t - 1)! orders.
 *    The current run counts only its rounds so far.
 * 4. The sum stops at k: once a run's player repeats an earlier one in the chain, or is j, that
 *    player can't arrive there (each player arrives once, and j arrives at i + 1), so every further
 *    term is 0. k = the latest runs that have distinct players and don't include j.
 *
 * O(n^2): each round updates every player once.
 */
fun eliminationXor(n: Int, a: IntArray): Long {
    val atPosition = fact[n - 1]                  // permutations with a given player at a given position
    val weakest = LongArray(n + 1) { atPosition } // round 1: c is whoever is first in line
    val runLength = IntArray(n + 1)               // runLength[r]: rounds in run r (current: so far)
    val lastRunOf = IntArray(n + 1)               // the latest run whose protected player is j, or 0
    val guardedUpTo = LongArray(n + 1)            // guardedUpTo[k]: step 3's sum over the k latest runs
    var runs = 0
    var oldestDistinctRun = 1                     // runs oldestDistinctRun..runs have distinct players
    var result = 0L

    for (round in 1 until n) {
        val x = a[round]
        if (round == 1 || x != a[round - 1]) {
            runs++
            runLength[runs] = 1
            oldestDistinctRun = maxOf(oldestDistinctRun, lastRunOf[x] + 1)
            lastRunOf[x] = runs
        } else {
            runLength[runs]++
        }
        val distinctRuns = runs - oldestDistinctRun + 1
        for (k in 1..distinctRuns) {
            val term = runLength[runs - k + 1] * fact[n - k - 1] % MOD
            guardedUpTo[k] = if (k % 2 == 1) (guardedUpTo[k - 1] + term) % MOD
            else (guardedUpTo[k - 1] - term + MOD) % MOD
        }
        for (j in 1..n) {
            if (j == x) {                         // protected: never eliminated this round
                weakest[j] = (weakest[j] + atPosition) % MOD
                continue
            }
            val guarded = guardedUpTo[minOf(distinctRuns, runs - lastRunOf[j])]
            val eliminated = (weakest[j] + guarded) % MOD
            result = result xor (eliminated * j * round)   // < 10^9 * 5000 * 5000: fits a Long
            weakest[j] = (atPosition - guarded + MOD) % MOD
        }
    }
    return result
}

fun solve() {
    val n = In.int()
    val a = IntArray(n)                           // a[1..n-1], as in the statement
    for (round in 1 until n) a[round] = In.int()
    out.append(eliminationXor(n, a)).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}