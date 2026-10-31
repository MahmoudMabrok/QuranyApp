#!/usr/bin/env bash
# Cold-start numbers for the signed release base APK.
# interpret-only = no AOT. speed-profile = ART compiles the shipped baseline profile.
# Median of 5 cold starts after one warmup. TotalTime is first window, not the 2s splash delay.
set -euo pipefail

APK="${1:?apk path}"
OUT="${2:?markdown out}"
PKG="education.mahmoud.quranyapp"
ACT="${PKG}/.feature.home_Activity.HomeActivity"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb missing" >&2
  exit 1
fi

sign_apk() {
  local src="$1"
  local dest="$2"
  local bt ks
  bt="$(find "${ANDROID_HOME:-$ANDROID_SDK_ROOT}/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)"
  ks="${RUNNER_TEMP:-/tmp}/qurany-debug.jks"
  if [[ ! -f "$ks" ]]; then
    keytool -genkeypair -keystore "$ks" -storepass android -alias androiddebugkey \
      -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
      -dname "CN=Qurany Perf,O=Qurany,C=US"
  fi
  "$bt/zipalign" -f -p 4 "$src" "${dest}.aligned"
  "$bt/apksigner" sign --ks "$ks" --ks-pass pass:android --key-pass pass:android \
    --out "$dest" "${dest}.aligned"
  rm -f "${dest}.aligned"
}

SIGNED="${RUNNER_TEMP:-/tmp}/qurany-release-signed.apk"
sign_apk "$APK" "$SIGNED"
adb install -r -d "$SIGNED"

median() {
  printf '%s\n' "$@" | awk 'NF { a[++n] = $1 } END {
    if (n == 0) { print "na"; exit }
    for (i = 1; i <= n; i++) for (j = i + 1; j <= n; j++) if (a[j] + 0 < a[i] + 0) { t = a[i]; a[i] = a[j]; a[j] = t }
    if (n % 2) print a[(n + 1) / 2]; else printf "%.0f\n", (a[n / 2] + a[n / 2 + 1]) / 2
  }'
}

measure() {
  local mode="$1"
  adb shell cmd package compile -m "$mode" -f "$PKG" >/dev/null
  adb shell am force-stop "$PKG" || true
  adb shell am start -W -n "$ACT" >/dev/null || true
  sleep 2
  local times=() t out i
  for i in 1 2 3 4 5; do
    adb shell am force-stop "$PKG" || true
    sleep 1
    out="$(adb shell am start -W -n "$ACT" || true)"
    printf '[%s #%s] %s\n' "$mode" "$i" "$(printf '%s' "$out" | tr '\n' ' ')" >&2
    t="$(printf '%s\n' "$out" | awk -F: '/TotalTime/ { gsub(/[^0-9]/, "", $2); print $2; exit }')"
    if [[ -n "${t:-}" ]]; then
      times+=("$t")
    fi
  done
  if [[ ${#times[@]} -eq 0 ]]; then
    echo "na"
  else
    median "${times[@]}"
  fi
}

INTERPRET="$(measure interpret-only)"
PROFILE="$(measure speed-profile)"

delta="—"
if [[ "$INTERPRET" != "na" && "$PROFILE" != "na" && "$INTERPRET" != "0" ]]; then
  delta="$(awk -v a="$PROFILE" -v b="$INTERPRET" 'BEGIN { printf "%+.1f%%", (a - b) / b * 100 }')"
fi

cat > "$OUT" <<EOF
<!-- qurany-startup-numbers -->
## وقت الإقلاع على المحاكي

قياس \`am start -W\` على API المحاكي، APK الـ release بعد التوقيع بـ v2. خمس إقلاعات باردة بعد warmup واحد، والرقم الوسيط لـ \`TotalTime\`.

| الوضع | الوسيط |
| --- | ---: |
| interpret-only (من غير AOT) | ${INTERPRET} ms |
| speed-profile (يترجم الـ baseline profile) | ${PROFILE} ms |
| الفرق | ${delta} |

\`speed-profile\` بيستخدم \`baseline.prof\` اللي جوه الـ APK. سالب يعني أسرع. أول فتحة بتستورد القرآن لـ Room ومش داخلة في الوسيط. تأخير الـ splash (2 ثانية) بعد أول فريم، فـ \`TotalTime\` مش مفروض يعدّه.
EOF

cat "$OUT"
