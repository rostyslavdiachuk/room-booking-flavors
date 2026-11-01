#!/usr/bin/env bash
# Один сценарій — п'ять архітектур. Вивід кожної має збігтися з expected.txt.
# Потрібен лише JDK 21 (Maven не потрібен).
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/out"
status=0
for flavor in layered layered-by-feature layered-ddd hexagonal clean; do
    main=ua.edu.booking.App
    [ "$flavor" = clean ] && main=ua.edu.booking.framework.Main
    rm -rf "$OUT/$flavor" && mkdir -p "$OUT/$flavor/classes" "$OUT/$flavor/run"
    javac -encoding UTF-8 -d "$OUT/$flavor/classes" $(find "$ROOT/$flavor/src/main" -name '*.java') || { status=1; continue; }
    (cd "$OUT/$flavor/run" && java -Dstdout.encoding=UTF-8 -cp "$OUT/$flavor/classes" $main \
        < "$ROOT/scripts/scenario.txt" > "$OUT/$flavor/actual.txt" 2>/dev/null)
    if diff "$OUT/$flavor/actual.txt" "$ROOT/scripts/expected.txt" > "$OUT/$flavor/diff.txt"; then
        printf '%-20s OK   (%s файлів)\n' "$flavor" "$(find "$ROOT/$flavor/src/main" -name '*.java' | wc -l | tr -d ' ')"
    else
        printf '%-20s DIFF (див. out/%s/diff.txt)\n' "$flavor" "$flavor"; status=1
    fi
done
exit $status
