#!/usr/bin/env bash
# Usage: scripts/validate.sh PROBLEM_DIR...
#
# Compiles each problem's Main.kt the way Codeforces does (kotlinc 2.2.0 on
# Java 11, from scripts/install-judge-kotlin.sh) and runs it on every
# non-empty input*.txt sample with the judge's JVM options.
#   - If the problem has a Check.kt, that checker judges the output
#     (for "print any valid answer" problems).
#   - Otherwise the output must match the paired expected*.txt
#     (input2.txt pairs with expected2.txt), ignoring whitespace amounts
#     and blank lines.
# Jars are cached in _build/ and rebuilt only when the source changes,
# because kotlinc takes several seconds per file.
# Exits non-zero if any sample fails.
set -u
CF_KOTLIN=${CF_KOTLIN:-$HOME/.local/cf-kotlin}
KOTLINC="$CF_KOTLIN/kotlinc/bin/kotlinc"
JAVA="$CF_KOTLIN/jdk/bin/java"
MEMORY_MB=${MEMORY_MB:-256}
if [ ! -x "$KOTLINC" ] || [ ! -x "$JAVA" ]; then
  echo "No toolchain in $CF_KOTLIN. Run: scripts/install-judge-kotlin.sh"; exit 1
fi
# The run command from Codeforces' command-line table for Kotlin 2.2.0.
run_jar() {
  "$JAVA" -XX:NewRatio=5 -Xms8M "-Xmx${MEMORY_MB}M" -Xss64M -DONLINE_JUDGE=true \
    -Duser.language=en -Duser.region=US -Duser.variant=US -jar "$@"
}
# compile SOURCE JAR: rebuild JAR only if it is missing or older than SOURCE.
compile() {
  [ "$2" -nt "$1" ] && return 0
  mkdir -p "$(dirname "$2")"
  rm -f "$2"
  local errors
  if ! errors=$(JAVA_HOME="$CF_KOTLIN/jdk" "$KOTLINC" "$1" -include-runtime -d "$2" 2>&1); then
    echo "$errors" | grep -v '^warning:'
    return 1
  fi
}

failed=0
for dir in "$@"; do
  dir=${dir%/}
  name=${dir#contests/}
  if [ ! -f "$dir/Main.kt" ]; then
    echo "⚠️  $name: no Main.kt, skipped (empty folder? recreate it with make add-problem)"
    continue
  fi
  if ! compile "$dir/Main.kt" "$dir/_build/main.jar"; then
    echo "❌ $name: does not compile"; failed=1; continue
  fi
  if [ -f "$dir/Check.kt" ] && ! compile "$dir/Check.kt" "$dir/_build/check.jar"; then
    echo "❌ $name: Check.kt does not compile"; failed=1; continue
  fi

  shopt -s nullglob
  inputs=("$dir"/input*.txt)
  shopt -u nullglob
  ran=0
  for input in "${inputs[@]}"; do
    [ -s "$input" ] || continue
    ran=1
    sample=$(basename "$input")
    actual=$(mktemp)
    if ! run_jar "$dir/_build/main.jar" < "$input" > "$actual"; then
      echo "❌ $name ($sample): runtime error (see the exception above)"
      failed=1; rm -f "$actual"; continue
    fi
    if [ -f "$dir/Check.kt" ]; then
      if verdict=$(run_jar "$dir/_build/check.jar" "$input" "$actual"); then
        echo "✅ $name ($sample, checker)"
      else
        echo "❌ $name ($sample, checker): $verdict"; failed=1
      fi
    else
      expected="$dir/expected${sample#input}"
      if [ ! -f "$expected" ]; then
        echo "❌ $name ($sample): missing $(basename "$expected")"; failed=1
      elif diff -u -b -B --label expected --label actual "$expected" "$actual"; then
        echo "✅ $name ($sample)"
      else
        echo "❌ $name ($sample)"; failed=1
      fi
    fi
    rm -f "$actual"
  done
  [ "$ran" = 1 ] || echo "⚠️  $name: no non-empty input*.txt, nothing to validate"
done
exit "$failed"
