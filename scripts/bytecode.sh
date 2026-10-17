#!/usr/bin/env bash
# Usage: scripts/bytecode.sh PROBLEM_DIR [CLASS] [full]
#
# Compiles PROBLEM_DIR/Main.kt with the judge's kotlinc 2.2.0 on Java 11 (from
# scripts/install-judge-kotlin.sh) and prints the bytecode with javap:
#   -c instructions, -p private members too, -l line-number and local-variable tables.
# CLASS picks the class: MainKt (default) holds solve and main; In is the input reader.
# full prints javap -v instead: class-file version, constant pool, stack map frames.
# Classes go to PROBLEM_DIR/_build/classes (ignored by git) and are rebuilt only
# when Main.kt changes, like validate.sh's jars.
set -euo pipefail
CF_KOTLIN=${CF_KOTLIN:-$HOME/.local/cf-kotlin}
dir=${1%/}
class=${2:-MainKt}
mode=${3:-}
[ -f "$dir/Main.kt" ] || { echo "No Main.kt in $dir"; exit 1; }
[ -x "$CF_KOTLIN/kotlinc/bin/kotlinc" ] || { echo "No toolchain in $CF_KOTLIN. Run: scripts/install-judge-kotlin.sh"; exit 1; }

out="$dir/_build/classes"
if [ ! "$out/MainKt.class" -nt "$dir/Main.kt" ]; then
  rm -rf "$out"
  mkdir -p "$out"
  # Show compile errors, not warnings. (Hiding all of stderr would hide the errors too.)
  if ! errors=$(JAVA_HOME="$CF_KOTLIN/jdk" "$CF_KOTLIN/kotlinc/bin/kotlinc" "$dir/Main.kt" -d "$out" 2>&1); then
    echo "$errors" | grep -v '^warning:'
    rm -rf "$out"
    exit 1
  fi
fi

file="$out/$class.class"
if [ ! -f "$file" ]; then
  echo "No class $class. Classes compiled from Main.kt:"
  for f in "$out"/*.class; do basename "$f" .class | sed 's/^/  /'; done
  exit 1
fi

javap="$CF_KOTLIN/jdk/bin/javap"
[ -x "$javap" ] || { echo "No javap in $CF_KOTLIN/jdk: it must be a full JDK, not just a Java runtime."; exit 1; }
if [ "$mode" = full ]; then "$javap" -v -p "$file"; else "$javap" -c -p -l "$file"; fi
