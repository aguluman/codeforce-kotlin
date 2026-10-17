SHELL := /bin/bash
.DEFAULT_GOAL := help
.PHONY: new-contest add-problem ide run-problem validate validate-all bytecode clean help

# Toolchain from scripts/install-judge-kotlin.sh: kotlinc 2.2.0 + Java 11.
CF_KOTLIN ?= $(HOME)/.local/cf-kotlin
export CF_KOTLIN
# Memory limit in MB for running solutions, as on the problem page; validate.sh reads it too.
MEMORY_MB ?= 256
export MEMORY_MB

# Every problem directory: contests/contest_N/X-title
ALL_PROBLEMS = $$(find contests -mindepth 2 -maxdepth 2 -type d | sort)

# Shell fragment: resolve CONTEST and PROBLEM into $problem_path, or exit.
define find_problem
if [ -z "$(CONTEST)" ] || [ -z "$(PROBLEM)" ]; then \
	echo "Usage: make $@ CONTEST=XXXX PROBLEM=A"; exit 1; fi; \
shopt -s nullglob; matches=(contests/contest_$(CONTEST)/$(PROBLEM)-*/); shopt -u nullglob; \
if [ $${#matches[@]} -eq 0 ]; then \
	echo "Problem directory not found: contests/contest_$(CONTEST)/$(PROBLEM)-*"; exit 1; fi; \
problem_path="$${matches[0]%/}"
endef

# make new-contest [CONTEST=XXXX]
new-contest:
	@contest="$(CONTEST)"; \
	[ -n "$$contest" ] || read -p "Enter contest number (e.g., 1136): " contest; \
	dir="contests/contest_$$contest"; \
	if [ -d "$$dir" ]; then echo "Contest $$contest already exists: $$dir"; exit 1; fi; \
	mkdir -p "$$dir"; \
	echo "✅ Created $$dir. Next: make add-problem CONTEST=$$contest"

# make add-problem CONTEST=XXXX [TITLE="Some Title"] [LETTER=C]
# LETTER defaults to the next unused letter; set it when you skip problems.
add-problem:
	@if [ -z "$(CONTEST)" ]; then echo "Please provide CONTEST=XXXX"; exit 1; fi; \
	contest_dir="contests/contest_$(CONTEST)"; \
	if [ ! -d "$$contest_dir" ]; then \
		echo "Contest directory not found: $$contest_dir (run: make new-contest CONTEST=$(CONTEST))"; exit 1; fi; \
	title="$(TITLE)"; [ -n "$$title" ] || read -p "Enter problem title (e.g., mexification): " title; \
	letter="$(LETTER)"; \
	if [ -z "$$letter" ]; then \
		shopt -s nullglob; dirs=("$$contest_dir"/*/); \
		letter=$$(printf "\\$$(printf '%03o' $$((65 + $${#dirs[@]})))"); \
	fi; \
	slug=$$(printf '%s' "$$title" | tr '[:upper:]' '[:lower:]' | tr -s ' _' '--'); \
	problem_dir="$$contest_dir/$$letter-$$slug"; \
	if compgen -G "$$contest_dir/$$letter-*" > /dev/null; then \
		echo "Problem $$letter already exists in $$contest_dir (pass LETTER=...)"; exit 1; fi; \
	mkdir -p "$$problem_dir"; \
	title_escaped=$$(printf '%s' "$$title" | sed 's/[\\&/]/\\&/g'); \
	sed -e "s/\[\[CONTEST\]\]/$(CONTEST)/g" -e "s/\[\[LETTER\]\]/$$letter/g" \
	    -e "s/\[\[TITLE\]\]/$$title_escaped/g" templates/Main.kt > "$$problem_dir/Main.kt"; \
	touch "$$problem_dir/input.txt" "$$problem_dir/expected.txt"; \
	echo "✅ Added problem $$letter: $$title"; \
	echo "   Source: $$problem_dir/Main.kt"; \
	scripts/ide.sh

# make run-problem CONTEST=XXXX PROBLEM=A [INPUT=path] [MEMORY_MB=256]
# Prints the program's output instead of comparing it. Runs with the judge's JVM options,
# the same as scripts/validate.sh: keep the two in step if Codeforces changes them.
# The old jar is deleted before compiling, so a failed compile can never run old code.
run-problem:
	@$(find_problem); \
	input="$(INPUT)"; if [ -z "$$input" ] || [ "$$input" = input ]; then input=input.txt; fi; \
	if [ -f "$$problem_path/$$input" ]; then input="$$problem_path/$$input"; \
	elif [ ! -f "$$input" ]; then echo "Input not found: $$input"; exit 1; fi; \
	jar="$$problem_path/_build/main.jar"; \
	if [ ! "$$jar" -nt "$$problem_path/Main.kt" ]; then \
		mkdir -p "$$problem_path/_build"; \
		rm -f "$$jar"; \
		JAVA_HOME="$(CF_KOTLIN)/jdk" "$(CF_KOTLIN)/kotlinc/bin/kotlinc" "$$problem_path/Main.kt" \
			-include-runtime -d "$$jar" 2>&1 | grep -v '^warning:'; \
		[ -f "$$jar" ] || exit 1; \
	fi; \
	"$(CF_KOTLIN)/jdk/bin/java" -XX:NewRatio=5 -Xms8M -Xmx$(MEMORY_MB)M -Xss64M -DONLINE_JUDGE=true \
		-Duser.language=en -Duser.region=US -Duser.variant=US -jar "$$jar" < "$$input"

# make ide: one IntelliJ module per problem (add-problem runs it for you)
ide:
	@scripts/ide.sh

# make validate CONTEST=XXXX PROBLEM=A
validate:
	@$(find_problem); scripts/validate.sh "$$problem_path"

validate-all:
	@scripts/validate.sh $(ALL_PROBLEMS)

# make bytecode CONTEST=XXXX PROBLEM=A [CLASS=In] [FULL=1]
# What the judge's compiler makes of Main.kt, as javap prints it. Pipe to less for long output.
bytecode:
	@$(find_problem); scripts/bytecode.sh "$$problem_path" "$(or $(CLASS),MainKt)" "$(if $(FULL),full,)"

clean:
	@find contests -type d -name _build -prune -exec rm -rf {} +; echo "✅ Clean complete"

help:
	@echo "Codeforces Kotlin workflow (submission is manual: upload Main.kt)"; \
	echo "  make new-contest [CONTEST=XXXX]"; \
	echo "  make add-problem CONTEST=XXXX [TITLE=...] [LETTER=C]"; \
	echo "  make ide   (regenerate IntelliJ modules, one per problem)"; \
	echo "  make run-problem CONTEST=XXXX PROBLEM=A [INPUT=file]"; \
	echo "  make validate CONTEST=XXXX PROBLEM=A"; \
	echo "  make bytecode CONTEST=XXXX PROBLEM=A [CLASS=In] [FULL=1]   (javap of the judge compiler's output)"; \
	echo "  make validate-all | make clean"
