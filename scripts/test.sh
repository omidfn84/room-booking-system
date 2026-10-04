#!/usr/bin/env bash
# Compiles src/, test/ and test-ai/ with plain javac and runs every *Test class
# with JUnit 4. Used both locally and by the GitHub Actions workflow.
#
#   scripts/test.sh            # run test/ and test-ai/
#   scripts/test.sh test       # run only the core suite
set -euo pipefail

cd "$(dirname "$0")/.."

JUNIT_VERSION=4.13.2
HAMCREST_VERSION=1.3
TEST_LIB=build/test-lib
OUT=build/classes
MAVEN=https://repo1.maven.org/maven2

mkdir -p "$TEST_LIB"
fetch() {
    local path=$1 file=$2
    [ -f "$TEST_LIB/$file" ] || curl -sSfL -o "$TEST_LIB/$file" "$MAVEN/$path/$file"
}
fetch "junit/junit/$JUNIT_VERSION" "junit-$JUNIT_VERSION.jar"
fetch "org/hamcrest/hamcrest-core/$HAMCREST_VERSION" "hamcrest-core-$HAMCREST_VERSION.jar"

if [ $# -gt 0 ]; then SUITES=("$@"); else SUITES=(test test-ai); fi
CP="lib/*:$TEST_LIB/*"

rm -rf "$OUT"
mkdir -p "$OUT"
find src "${SUITES[@]}" -name '*.java' > build/sources.txt
javac -encoding UTF-8 -d "$OUT" -cp "$CP" @build/sources.txt

TESTS=$(for s in "${SUITES[@]}"; do (cd "$s" && find . -name '*Test.java'); done \
    | sed -e 's|^\./||' -e 's|\.java$||' -e 's|/|.|g' | sort)

echo "Running $(echo "$TESTS" | wc -l | tr -d ' ') test classes from: ${SUITES[*]}"
java --enable-native-access=ALL-UNNAMED -cp "$OUT:$CP" org.junit.runner.JUnitCore $TESTS
