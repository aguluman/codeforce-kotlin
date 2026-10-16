# Kotlin Codeforces Workspace

A workspace for solving Codeforces problems in Kotlin, set up so that a
solution passing `make validate` compiles and runs the way the judge runs it.

Codeforces offers **Kotlin 2.2.0**. It compiles with `kotlinc` and runs on
**Java 11**. `make validate` uses exactly that: kotlinc 2.2.0 on a private Java
11, with the judge's JVM options. See [docs/kotlin-on-codeforces.md](docs/kotlin-on-codeforces.md)
for why the Java version matters and for measured I/O speeds.

## Setup

You need bash, make, curl, unzip and tar. Linux and macOS work directly; on
Windows, use WSL.

```bash
scripts/install-judge-kotlin.sh     # installs to ~/.local/cf-kotlin
```

The script downloads kotlinc 2.2.0 and the Eclipse Temurin JDK 11.0.28+6 into
`~/.local/cf-kotlin` (about 400 MB). It does not change your own Java, Kotlin or
`PATH`.

If you already have a Java 11 JDK (any vendor, for example Amazon Corretto),
pass its home folder and only kotlinc is downloaded (about 80 MB):

```bash
JDK11_HOME=/path/to/corretto-11 scripts/install-judge-kotlin.sh
```

The home folder is the one containing `bin/java` and `bin/javac`. The script
checks that it is Java 11 and refuses anything newer, because the judge runs
Java 11. Your other JDKs are not affected, and `make validate` uses only this
one. If you later move or upgrade that JDK, run the command again.

To find the folder: on macOS, `/usr/libexec/java_home -v 11` prints it. On
Linux, look in `/usr/lib/jvm/`. JDKs downloaded by IntelliJ IDEA are usually in
`~/.jdks/` (on macOS, `~/Library/Java/JavaVirtualMachines/`, with the home
folder inside at `Contents/Home`). Or search for it:
`find ~ /usr/lib/jvm /Library/Java -name javac -path '*11*' 2>/dev/null`
prints `<home>/bin/javac`. To remove it: `rm -rf ~/.local/cf-kotlin`. To install elsewhere, pass a
folder (`scripts/install-judge-kotlin.sh /path`) and then use `CF_KOTLIN=/path`
with `make`.

## Workflow

```bash
make new-contest CONTEST=1500
make add-problem CONTEST=1500 TITLE="Binary Search"      # next letter, A, B, ...
make add-problem CONTEST=1500 TITLE="Hard One" LETTER=D  # skipped a problem
```

Each problem folder gets a `Main.kt` from the template, plus empty `input.txt`
and `expected.txt`. Paste the sample input and output from the problem page
into those, write the solution, then:

```bash
make validate CONTEST=1500 PROBLEM=A      # compile and check every sample
make run-problem CONTEST=1500 PROBLEM=A   # just print the output
make validate-all                         # every problem
make clean                                # delete compiled jars
```

Submit only `Main.kt`, with **Kotlin 2.2.0** selected.

### Samples and checkers

Validation runs every non-empty `input*.txt` and compares it with the matching
`expected*.txt` (`input2.txt` pairs with `expected2.txt`). Differences in the
amount of whitespace and blank lines are ignored. Add your own test files the
same way, for example one with maximum-size values to check for overflow.

Some problems accept any valid answer, so comparing with the sample output is
the wrong test. Add a `Check.kt` with a `main(args)` that reads the input file
`args[0]` and your output `args[1]`, and exits non-zero on a wrong answer.
Validation will use it instead of `expected.txt`. See
`contests/contest_1047/A-collatz-conjecture/Check.kt`.

### Speed

kotlinc takes several seconds per file, so compiled jars are kept in each
problem's `_build/` folder and rebuilt only when `Main.kt` changes.

### IntelliJ

Open the folder in IntelliJ IDEA. The project uses Corretto 11 as its SDK and
attaches `kotlin-stdlib.jar` from `~/.local/cf-kotlin`, so run the install script
first.

Every problem is its own IntelliJ module, like a directory with its own `dune`
file: `contests/contest_1047/A-collatz-conjecture/cf-1047-A-collatz-conjecture.iml`.
Each `Main.kt` declares the same top-level `In`, `out`, `solve` and `main`, and
Kotlin folders don't separate names, so in one shared module they would clash.
Separate modules keep them apart, and every problem gets full analysis.

`make add-problem` creates the new module for you. After adding or deleting
problem folders by hand, run `make ide`. The IntelliJ Run button uses your
default JDK without the judge's options, so trust `make validate`, not a green run.

## The template

`templates/Main.kt` has a fast byte-level reader (`In.int()`, `In.long()`,
`In.word()`, `In.ints(n)`, `In.longs(n)`) and collects output in a
`StringBuilder` called `out`, printed once at the end. It loops over `t` test
cases; for a single-case problem, replace the loop with `solve()`.
