#!/usr/bin/env bash
# Installs the Codeforces Kotlin 2.2.0 toolchain into a private folder:
#   - kotlinc 2.2.0, the compiler Codeforces uses
#   - Eclipse Temurin JDK 11.0.28+6, matching the judge's Java 11 runtime
# Nothing outside the folder is changed; your own Java and Kotlin stay as they are.
#
# Usage: scripts/install-judge-kotlin.sh [PREFIX]   (default ~/.local/cf-kotlin)
# Needs bash, curl, unzip and tar. Supports Linux and macOS on x86-64 or ARM;
# on Windows, run it inside WSL.
set -euo pipefail
prefix=${1:-$HOME/.local/cf-kotlin}
kotlin_version=2.2.0
jdk_version=11.0.28+6

case "$(uname -s)" in
  Linux) os=linux ;;
  Darwin) os=mac ;;
  *) echo "Unsupported OS $(uname -s): use Linux, macOS, or WSL on Windows"; exit 1 ;;
esac
case "$(uname -m)" in
  x86_64 | amd64) arch=x64 ;;
  arm64 | aarch64) arch=aarch64 ;;
  *) echo "Unsupported CPU $(uname -m)"; exit 1 ;;
esac

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir -p "$prefix"

echo "==> download kotlinc $kotlin_version"
curl -fsSL -o "$work/kotlinc.zip" \
  "https://github.com/JetBrains/kotlin/releases/download/v$kotlin_version/kotlin-compiler-$kotlin_version.zip"
rm -rf "$prefix/kotlinc"
unzip -q "$work/kotlinc.zip" -d "$prefix"

echo "==> download JDK $jdk_version ($os, $arch)"
tag="jdk-${jdk_version/+/%2B}"
file="OpenJDK11U-jdk_${arch}_${os}_hotspot_${jdk_version/+/_}.tar.gz"
curl -fsSL -o "$work/jdk.tar.gz" \
  "https://github.com/adoptium/temurin11-binaries/releases/download/$tag/$file"
rm -rf "$prefix/jdk-$jdk_version" "$prefix/jdk"
tar xzf "$work/jdk.tar.gz" -C "$prefix"
# macOS archives keep the JDK under Contents/Home.
home="$prefix/jdk-$jdk_version"
[ -d "$home/Contents/Home" ] && home="$home/Contents/Home"
ln -s "$home" "$prefix/jdk"

echo "==> check"
JAVA_HOME="$prefix/jdk" "$prefix/kotlinc/bin/kotlinc" -version 2>&1
"$prefix/jdk/bin/java" -version 2>&1 | head -1
echo "✅ Installed in $prefix"
