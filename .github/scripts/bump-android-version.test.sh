#!/usr/bin/env bash
# Exercises bump_android_version without touching app/build.gradle.
set -euo pipefail

root=$(cd "$(dirname "$0")" && pwd)
# shellcheck disable=SC1091
source "$root/bump-android-version.sh"

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

cat > "$tmp/groovy.gradle" <<'EOF'
    defaultConfig {
        versionCode 31
        versionName "3.0.1"
    }
EOF

cat > "$tmp/kts.gradle" <<'EOF'
        versionCode = 40
        versionName = "1.2.3"
EOF

unset VERSION_NAME
GRADLE_FILE="$tmp/groovy.gradle" VERSION_CODE_STEP=1 bump_android_version
test "$CURRENT_CODE" = "31"
test "$NEW_CODE" = "32"
test "$VERSION_NAME" = "3.0.1"
grep -q 'versionCode 32' "$tmp/groovy.gradle"
grep -q 'versionName "3.0.1"' "$tmp/groovy.gradle"

GRADLE_FILE="$tmp/kts.gradle" VERSION_CODE_STEP=1 VERSION_NAME="9.9.9" bump_android_version
test "$NEW_CODE" = "41"
grep -q 'versionCode = 41' "$tmp/kts.gradle"
grep -q 'versionName = "9.9.9"' "$tmp/kts.gradle"

# A name-only update must not move versionCode (Play deploy commits the name this way).
GRADLE_FILE="$tmp/groovy.gradle" VERSION_CODE_STEP=0 VERSION_NAME="3.0.2" bump_android_version
test "$CURRENT_CODE" = "32"
test "$NEW_CODE" = "32"
grep -q 'versionCode 32' "$tmp/groovy.gradle"
grep -q 'versionName "3.0.2"' "$tmp/groovy.gradle"

echo "bump-android-version tests passed"
