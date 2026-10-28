// Codeforces 2276 — Problem D: Caveman

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

/** What Monocarp can say. There is no One: valid colourings come in pairs, so 1 can't happen. */
enum class Verdict(val text: String) {
    BrainMelt("Brain Melt"),
    Two("2"),
    Many("Many")
}

/**
 * Classifies a graph from its vertex degrees alone. Proof, case by case:
 *
 * - Pairs: swapping every colour keeps a colouring valid, so the count is even and never 1.
 * - BrainMelt: a vertex with no neighbours can never have one of the opposite colour. With no
 *   such vertex, every component has a spanning tree, and colouring that tree in alternating
 *   colours is valid (extra edges only add neighbours), so at least one colouring exists.
 * - Two components or more: each swaps independently, so there are at least 2 * 2 = 4.
 * - One component that is not a star: some spanning tree has two adjacent vertices that both
 *   have other neighbours (or the graph is a triangle, with 6 colourings). Give those two the
 *   same colour and alternate outwards from each: still valid, and a 3rd and 4th colouring.
 * - A star: each leaf's only neighbour is the centre, so it must take the opposite colour.
 *   Only the centre chooses, so there are exactly 2.
 */
fun classify(n: Int, m: Int, degree: IntArray): Verdict {
    val hasLonelyVertex =
        degree.any { it == 0 }
    val isStar =
        m == n - 1 && degree.any { it == n - 1 }   // a centre joined to all, nothing else
    return when {
        hasLonelyVertex -> Verdict.BrainMelt
        isStar          -> Verdict.Two
        else            -> Verdict.Many
    }
}

fun solve() {
    val n = In.int()
    val m = In.int()
    val degree = IntArray(n) // every edge must still be read, or the next test starts mid-input

    repeat(m) {
        degree[In.int() - 1]++   // vertices are numbered from 1
        degree[In.int() - 1]++
    }

    out.append(classify(n, m, degree).text).append('\n')
}

fun main() {
    repeat(In.int()) { solve() }
    print(out)
}
