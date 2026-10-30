#!/usr/bin/env bash
# Increment versionCode and set versionName in an Android Gradle file.
# Supports Groovy (`versionCode 31`, `versionName "1.0"`) and Kotlin DSL
# (`versionCode = 31`, `versionName = "1.0"`), including mixed spacing.
#
# Expects VERSION_NAME in the environment. Optional GRADLE_FILE
# (default: app/build.gradle). On success, sets CURRENT_CODE and NEW_CODE
# in the caller when this file is sourced.

bump_android_version() {
  local gradle_file="${GRADLE_FILE:-app/build.gradle}"
  local version_name="${VERSION_NAME-}"

  if [ -z "$version_name" ]; then
    echo "VERSION_NAME is required" >&2
    return 1
  fi
  if [ ! -f "$gradle_file" ]; then
    echo "Gradle file not found: $gradle_file" >&2
    return 1
  fi

  local current_code
  current_code=$(grep -m 1 -E '^[[:space:]]*versionCode[[:space:]]*=?[[:space:]]*[0-9]+' "$gradle_file" | grep -oE '[0-9]+' || true)
  if [ -z "$current_code" ]; then
    echo "Could not read versionCode from $gradle_file (expected 'versionCode 31' or 'versionCode = 31')" >&2
    return 1
  fi

  if ! grep -qE '^[[:space:]]*versionName[[:space:]]*=?[[:space:]]*"[^"]*"' "$gradle_file"; then
    echo "Could not read versionName from $gradle_file (expected 'versionName \"1.0\"' or 'versionName = \"1.0\"')" >&2
    return 1
  fi

  local new_code=$((current_code + 1))
  # Escape sed replacement metacharacters so a version name cannot alter the edit.
  local escaped_name
  escaped_name=$(printf '%s\n' "$version_name" | sed -e 's/[&/\]/\\&/g')

  sed -i -E "s/^([[:space:]]*versionCode[[:space:]]*=?[[:space:]]*)[0-9]+/\1${new_code}/" "$gradle_file"
  sed -i -E "s/^([[:space:]]*versionName[[:space:]]*=?[[:space:]]*)\"[^\"]*\"/\1\"${escaped_name}\"/" "$gradle_file"

  local updated_code updated_name
  updated_code=$(grep -m 1 -E '^[[:space:]]*versionCode[[:space:]]*=?[[:space:]]*[0-9]+' "$gradle_file" | grep -oE '[0-9]+' || true)
  updated_name=$(grep -m 1 -E '^[[:space:]]*versionName[[:space:]]*=?[[:space:]]*"[^"]*"' "$gradle_file" | sed -E 's/^[^"]*"([^"]*)".*/\1/' || true)

  if [ "$updated_code" != "$new_code" ] || [ "$updated_name" != "$version_name" ]; then
    echo "Version bump failed in $gradle_file. versionCode='${updated_code}' (expected ${new_code}), versionName='${updated_name}' (expected ${version_name})" >&2
    return 1
  fi

  CURRENT_CODE=$current_code
  NEW_CODE=$new_code
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  set -euo pipefail
  bump_android_version
fi
