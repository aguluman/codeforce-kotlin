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
`PATH`. To remove it: `rm -rf ~/.local/cf-kotlin`. To install elsewhere, pass a
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

## The template

`templates/Main.kt` has a fast byte-level reader (`In.int()`, `In.long()`,
`In.word()`, `In.ints(n)`, `In.longs(n)`) and collects output in a
`StringBuilder` called `out`, printed once at the end. It loops over `t` test
cases; for a single-case problem, replace the loop with `solve()`.
