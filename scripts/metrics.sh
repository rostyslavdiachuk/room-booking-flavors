#!/usr/bin/env bash
# Метрики компонентів (Ca, Ce, I, A, D і цикли) для кожного смаку.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
for flavor in ${@:-layered layered-by-feature layered-ddd hexagonal clean}; do
    echo; echo "################ $flavor"
    java -Dstdout.encoding=UTF-8 "$ROOT/tools/PackageMetrics.java" "$ROOT/$flavor/src/main/java"
done
