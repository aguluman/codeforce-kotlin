// Checker for 1047 A: any start value is accepted if k Collatz steps take it
// to x. Run by scripts/validate.sh as: java -jar check.jar input output
import java.io.File

fun main(args: Array<String>) {
    val input = File(args[0]).readText().trim().split(Regex("\\s+")).map { it.toLong() }
    val output = File(args[1]).readText().trim().split(Regex("\\s+")).map { it.toLong() }
    val t = input[0].toInt()
    for (case in 0 until t) {
        val k = input[1 + 2 * case]
        val x = input[2 + 2 * case]
        val v = output.getOrNull(case) ?: fail(case, "missing answer")
        var r = v
        repeat(k.toInt()) { r = if (r % 2 == 0L) r / 2 else 3 * r + 1 }
        if (v < 1 || r != x) fail(case, "$v reaches $r after $k steps, want $x")
    }
}

fun fail(case: Int, message: String): Nothing {
    println("case ${case + 1}: $message")
    kotlin.system.exitProcess(1)
}
