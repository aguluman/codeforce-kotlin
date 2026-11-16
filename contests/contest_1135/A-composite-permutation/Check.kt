// Checker for 1135 A: any permutation is accepted if every i * p[i] is
// composite; -1 is required exactly when n < 4, where position 1 cannot be
// paired with a composite value. Run by scripts/validate.sh as:
// java -jar check.jar input output
import java.io.File

fun main(args: Array<String>) {
    val input = File(args[0]).readText().trim().split(Regex("\\s+")).map { it.toInt() }
    val output = File(args[1]).readText().trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val t = input[0]
    var at = 0
    for (case in 0 until t) {
        val n = input[1 + case]
        fun next(): Int = output.getOrNull(at++)?.toIntOrNull() ?: fail(case, "missing or non-integer token")
        if (n < 4) {
            val v = next()
            if (v != -1) fail(case, "printed $v, want -1 since n=$n has no solution")
            continue
        }
        val seen = BooleanArray(n + 1)
        for (i in 1..n) {
            val v = next()
            if (v !in 1..n) fail(case, "p[$i]=$v is outside 1..$n")
            if (seen[v]) fail(case, "$v appears twice")
            seen[v] = true
            if (!composite(i * v)) fail(case, "$i * $v = ${i * v} is not composite")
        }
    }
    if (at != output.size) fail(t - 1, "extra output after the last case")
}

fun composite(x: Int): Boolean = x >= 4 && (2..x).takeWhile { it * it <= x }.any { x % it == 0 }

fun fail(case: Int, message: String): Nothing {
    println("case ${case + 1}: $message")
    kotlin.system.exitProcess(1)
}
