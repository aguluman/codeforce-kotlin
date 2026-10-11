# Kotlin on Codeforces

Codeforces' command-line table lists Kotlin 2.2.0 as:

```
compile: kotlinc %1
run:     java -XX:NewRatio=5 -Xms8M -Xmx{MEMORY_LIMIT_MB}M -Xss64M -DONLINE_JUDGE=true
              -Duser.language=en -Duser.region=US -Duser.variant=US -jar %1
```

where `java` is Java 11 (the table's `{JAVA11_32_HOME}`). `make validate`
reproduces this: kotlinc 2.2.0, Java 11, the same JVM options. Everything
below was measured or tested with that setup unless it says otherwise.

## The Java 11 trap

Kotlin code can call any Java library method, and kotlinc checks those calls
against the Java it runs on. With a newer Java, a method added after Java 11
compiles and runs locally, then fails on the judge:

```kotlin
val xs = java.util.stream.Stream.of(3, 1, 2).toList()   // Java 16+
```

Compiled with Java 21, this runs fine on Java 21, and on Java 11 it throws
`java.lang.NoSuchMethodError: 'java.util.List java.util.stream.Stream.toList()'`,
which Codeforces reports as a runtime error.

`make validate` compiles with the Java 11 from `scripts/install-judge-kotlin.sh`,
so the same line is a compile error on your machine instead. The message is not
obvious: `none of the following candidates is applicable`. When you see it for a
Java method, the method probably does not exist in Java 11.

Kotlin's own standard library is not affected: `-include-runtime` puts the
2.2.0 standard library into the jar, so every Kotlin 2.2 function works.

## Fast input and output

Reading 2,000,000 integers (about 20 MB):

| Method | Time |
|---|---|
| `readLine()` + `split(" ")` | 931 ms |
| `BufferedReader` + `StringTokenizer` | 228 ms |
| Byte-level reader (the template's `In`) | 54 ms |

Writing 200,000 lines:

| Method | Time |
|---|---|
| `println` for each line | 239 ms |
| Append to a `StringBuilder`, print once (the template's `out`) | 44 ms |

Measured on Java 11 with the judge's JVM options, on a Linux machine. The judge's
times will differ, but the ratios show which methods to avoid.

## Integer overflow

`Int` is 32-bit (up to 2,147,483,647) and overflows silently. `IntArray.sum()`
returns an `Int`, so summing large values overflows too. Use `Long` (`In.long()`,
`sumOf { it.toLong() }`) whenever a value can pass about 2 × 10^9.

## Evaluation order

Kotlin evaluates arguments left to right, so `Pair(In.int(), In.int())` reads
the first value first. (OCaml does the opposite, which caused a real bug in the
OCaml version of this project.)

## Stack and memory

The judge runs with `-Xss64M`, so recursion can go fairly deep, and `make
validate` uses the same option. `-Xmx` is the problem's memory limit; validation
uses 256 MB by default. For a problem with a different limit:
`make validate CONTEST=XXXX PROBLEM=A MEMORY_MB=512`.

## Sorting primitive arrays (not reproduced here)

Codeforces users widely report that `IntArray.sort()` and `LongArray.sort()`,
which use Java's dual-pivot quicksort, can be pushed to quadratic time by
specially crafted tests ("anti-quicksort" hacks). I have not reproduced this.
The usual precautions are to shuffle the array before sorting, or to sort a
`List<Int>` or `Array<Int>`, which use a merge sort with a guaranteed O(n log n).
