#!/usr/bin/env bash
# Compiles the project and starts the web version at http://localhost:8080
#
#   scripts/run-web.sh                       # public-demo mode (one-click demo accounts)
#   SCHEDULER_DEMO=false scripts/run-web.sh  # full mode: real sign-up, data in data/scheduler.db
#   PORT=9000 scripts/run-web.sh             # another port
set -euo pipefail                            # stop at the first error instead of carrying on

cd "$(dirname "$0")/.."                      # always run from the project folder

OUT=build/web-classes                        # separate from build/classes, which scripts/test.sh wipes
mkdir -p "$OUT"
find src -name '*.java' > build/web-sources.txt                          # list every source file
javac -encoding UTF-8 -d "$OUT" -cp "lib/*" @build/web-sources.txt       # compile them, with the jars in lib/ available

# exec replaces this script with the Java process, so Ctrl+C stops the server directly.
# --enable-native-access silences a SQLite warning on newer Java versions.
exec java --enable-native-access=ALL-UNNAMED -cp "$OUT:lib/*" scheduler.web.WebMain
