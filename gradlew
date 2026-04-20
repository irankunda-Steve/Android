#!/usr/bin/env sh
set -e
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle is not installed. Please install Gradle 8.14.3+ or add a wrapper." >&2
exit 1
