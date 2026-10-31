#!/usr/bin/env bash
# Build a single sideloadable APK from an app bundle, with on-demand modules
# that set dist:fusing include=true (including :quran_asr) merged in.
#
# Usage: build-universal-apk.sh <debug|release>
# The matching AAB must already exist (./gradlew :app:bundleDebug or bundleRelease).
# Release signing uses ./release.keystore plus KEYSTORE_PASSWORD, KEY_ALIAS, and
# KEY_PASSWORD. Debug signing uses the Android debug keystore.
set -euo pipefail

BUILD_TYPE="${1:-}"
if [[ "$BUILD_TYPE" != "debug" && "$BUILD_TYPE" != "release" ]]; then
  echo "Usage: $0 <debug|release>" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
mkdir -p "$ANDROID_HOME"

install_build_tools() {
  local sdkmanager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
  if [[ ! -x "$sdkmanager" ]]; then
    echo "Installing Android cmdline-tools..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdtools.zip
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    unzip -q /tmp/cmdtools.zip -d "$ANDROID_HOME/cmdline-tools"
    if [[ -d "$ANDROID_HOME/cmdline-tools/cmdline-tools" ]]; then
      rm -rf "$ANDROID_HOME/cmdline-tools/latest"
      mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    fi
    sdkmanager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
  fi
  yes | "$sdkmanager" --licenses >/dev/null || true
  echo "Installing build-tools;35.0.0..."
  "$sdkmanager" "build-tools;35.0.0"
}

BUILD_TOOLS="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -1 || true)"
if [[ -z "$BUILD_TOOLS" || ! -x "$BUILD_TOOLS/apksigner" ]]; then
  install_build_tools
  BUILD_TOOLS="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)"
fi
if [[ -z "$BUILD_TOOLS" || ! -x "$BUILD_TOOLS/apksigner" ]]; then
  echo "Failed to locate apksigner" >&2
  exit 1
fi
echo "Using build-tools: $BUILD_TOOLS"

AAB="app/build/outputs/bundle/${BUILD_TYPE}/app-${BUILD_TYPE}.aab"
if [[ ! -f "$AAB" ]]; then
  echo "App bundle not found at $AAB" >&2
  find app/build/outputs -name '*.aab' -o -name '*.apk' || true
  exit 1
fi

BUNDLETOOL_VERSION="1.18.3"
BUNDLETOOL_JAR="${RUNNER_TEMP:-/tmp}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
if [[ ! -f "$BUNDLETOOL_JAR" ]]; then
  curl -fsSL -o "$BUNDLETOOL_JAR" \
    "https://github.com/google/bundletool/releases/download/${BUNDLETOOL_VERSION}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
fi

if [[ "$BUILD_TYPE" == "release" ]]; then
  KS_FILE="${KEYSTORE_PATH:-$ROOT/release.keystore}"
  KS_ALIAS="${KEY_ALIAS:-}"
  KS_STORE_PASS="${KEYSTORE_PASSWORD:-}"
  KS_KEY_PASS="${KEY_PASSWORD:-}"
  if [[ ! -f "$KS_FILE" || -z "$KS_ALIAS" || -z "$KS_STORE_PASS" || -z "$KS_KEY_PASS" ]]; then
    echo "Release universal APK needs release.keystore, KEY_ALIAS, KEYSTORE_PASSWORD, and KEY_PASSWORD" >&2
    exit 1
  fi
else
  KS_FILE="${HOME}/.android/debug.keystore"
  KS_ALIAS="androiddebugkey"
  KS_STORE_PASS="android"
  KS_KEY_PASS="android"
  if [[ ! -f "$KS_FILE" ]]; then
    mkdir -p "${HOME}/.android"
    keytool -genkeypair -keystore "$KS_FILE" -storepass "$KS_STORE_PASS" \
      -alias "$KS_ALIAS" -keypass "$KS_KEY_PASS" -keyalg RSA -keysize 2048 \
      -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
  fi
fi

KS_PASS_FILE="$(mktemp)"
KEY_PASS_FILE="$(mktemp)"
APKS="$(mktemp --suffix=.apks)"
cleanup() {
  rm -f "$KS_PASS_FILE" "$KEY_PASS_FILE" "$APKS"
}
trap cleanup EXIT
printf '%s' "$KS_STORE_PASS" > "$KS_PASS_FILE"
printf '%s' "$KS_KEY_PASS" > "$KEY_PASS_FILE"

echo "Building universal APK from $AAB"
java -jar "$BUNDLETOOL_JAR" build-apks \
  --bundle="$AAB" \
  --output="$APKS" \
  --mode=universal \
  --overwrite \
  --ks="$KS_FILE" \
  --ks-pass="file:$KS_PASS_FILE" \
  --ks-key-alias="$KS_ALIAS" \
  --key-pass="file:$KEY_PASS_FILE"

OUT_DIR="app/build/outputs/apk/${BUILD_TYPE}"
OUT_APK="${OUT_DIR}/app-${BUILD_TYPE}-universal.apk"
mkdir -p "$OUT_DIR"
unzip -p "$APKS" universal.apk > "$OUT_APK"

echo "Verifying signature..."
"$BUILD_TOOLS/apksigner" verify --verbose "$OUT_APK"

python3 - "$OUT_APK" <<'PY'
import sys
import zipfile

apk = sys.argv[1]
needle = b"education/mahmoud/quranyapp/asr/ui/RecitationTrackingActivity"
fused = "quran_asr".encode("utf-16le")
with zipfile.ZipFile(apk) as z:
    names = z.namelist()
    tokens = [n for n in names if n.endswith("tokens.txt")]
    dex_hit = False
    for name in names:
        if name.startswith("classes") and name.endswith(".dex"):
            if needle in z.read(name):
                dex_hit = True
                break
    manifest = z.read("AndroidManifest.xml") if "AndroidManifest.xml" in names else b""
    manifest_hit = fused in manifest or b"quran_asr" in manifest
print(f"tokens.txt entries: {tokens or 'NONE'}")
print(f"feature class in dex: {dex_hit}")
print(f"quran_asr in manifest: {manifest_hit}")
if not tokens or not dex_hit:
    sys.exit("universal APK is missing fused quran_asr code or assets")
PY

echo "Universal APK ready: $OUT_APK"
