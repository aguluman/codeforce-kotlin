#!/usr/bin/env bash
# Usage: scripts/ide.sh
#
# Makes every problem its own IntelliJ module, the way each directory in a
# dune project has its own dune file. Every Main.kt declares the same
# top-level In, out, solve and main in the default package (Codeforces wants
# one self-contained file), and Kotlin folders don't separate names, so one
# shared module would report "Redeclaration" in every file. Separate modules
# keep each problem's names apart, and IntelliJ analyses them all at once.
#
# Writes <problem>/cf-<contest>-<problem>.iml when it is missing, and always
# rewrites .idea/modules.xml from the problem folders that exist now.
# Safe to run any number of times.
set -euo pipefail
cd "$(dirname "$0")/.."

# write_module DIR NAME: one module whose source root is DIR.
write_module() {
  local file="$1/$2.iml"
  [ -f "$file" ] && return 0
  cat > "$file" << 'IML'
<?xml version="1.0" encoding="UTF-8"?>
<module type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://$MODULE_DIR$">
      <sourceFolder url="file://$MODULE_DIR$" isTestSource="false" />
      <excludeFolder url="file://$MODULE_DIR$/_build" />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="library" name="kotlin-stdlib" level="project" />
  </component>
</module>
IML
  echo "  + module $2"
}

# The root module holds the scripts, docs and Makefile; it has no sources.
cat > codeforces-kotlin.iml << 'IML'
<?xml version="1.0" encoding="UTF-8"?>
<module type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://$MODULE_DIR$" />
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
  </component>
</module>
IML

entries=("codeforces-kotlin.iml")
write_module templates template
entries+=("templates/template.iml")

for dir in $(find contests -mindepth 2 -maxdepth 2 -type d | sort); do
  [ -f "$dir/Main.kt" ] || continue
  contest=$(basename "$(dirname "$dir")"); contest=${contest#contest_}
  name="cf-$contest-$(basename "$dir")"
  write_module "$dir" "$name"
  entries+=("$dir/$name.iml")
done

mkdir -p .idea
{
  echo '<?xml version="1.0" encoding="UTF-8"?>'
  echo '<project version="4">'
  echo '  <component name="ProjectModuleManager">'
  echo '    <modules>'
  for e in "${entries[@]}"; do
    echo "      <module fileurl=\"file://\$PROJECT_DIR\$/$e\" filepath=\"\$PROJECT_DIR\$/$e\" />"
  done
  echo '    </modules>'
  echo '  </component>'
  echo '</project>'
} > .idea/modules.xml
echo "✅ ${#entries[@]} IntelliJ modules in .idea/modules.xml"
