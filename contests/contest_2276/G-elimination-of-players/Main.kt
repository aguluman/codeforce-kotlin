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

const val MAX_N = 5000

/** A residue modulo 998244353; its operations always reduce, so an unreduced value cannot exist. */
@JvmInline
value class Mod private constructor(val residue: Long) {
    operator fun plus(other: Mod) = Mod((residue + other.residue).let { if (it >= MODULUS) it - MODULUS else it })
    operator fun minus(other: Mod) = Mod((residue - other.residue).let { if (it < 0) it + MODULUS else it })
    operator fun times(other: Mod) = Mod(residue * other.residue % MODULUS)

    /** Residues stored unboxed in a LongArray: reading and writing never allocate. */
    @JvmInline
    value class Array private constructor(private val residues: LongArray) {
        constructor(size: Int, initial: Mod) : this(LongArray(size) { initial.residue })
        operator fun get(i: Int) = Mod(residues[i])
        operator fun set(i: Int, value: Mod) { residues[i] = value.residue }
    }

    companion object {
        const val MODULUS = 998_244_353L
        val ZERO = Mod(0)
        fun of(value: Long) = Mod(Math.floorMod(value, MODULUS))
    }
}

val fact = Mod.Array(MAX_N + 1, Mod.of(1)).also { f ->
    for (i in 1..MAX_N) f[i] = f[i - 1] * Mod.of(i.toLong())
}

/** XOR over players j and rounds i of ans(j, i) * j * i, where ans = weakest(j) + guarded(j) each round. */
fun eliminationXor(n: Int, a: IntArray): Long {
    val atPosition = fact[n - 1]
    val weakest = Mod.Array(n + 1, atPosition)
    val runLength = IntArray(n + 1)
    val lastRunOf = IntArray(n + 1)
    val guardedUpTo = Mod.Array(n + 1, Mod.ZERO)
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
        } else {
            runLength[runs]++
        }
        val distinctRuns = runs - oldestDistinctRun + 1
        for (k in 1..distinctRuns) {
            val term = Mod.of(runLength[runs - k + 1].toLong()) * fact[n - k - 1]
            guardedUpTo[k] = if (k % 2 == 1) guardedUpTo[k - 1] + term else guardedUpTo[k - 1] - term
        }
        for (j in 1..n) {
            if (j == x) {
                weakest[j] = weakest[j] + atPosition
                continue
            }
            val guarded = guardedUpTo[minOf(distinctRuns, runs - lastRunOf[j])]
            result = result xor ((weakest[j] + guarded).residue * j * round)
            weakest[j] = atPosition - guarded
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
