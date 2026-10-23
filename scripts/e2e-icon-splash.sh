#!/usr/bin/env bash
# E2E check for the launcher icon and the launch splash screen.
#
# Installs the debug APK on a device, then captures:
#   build/e2e/icon-splash/app-info.png     the app's icon as the system shows it
#   build/e2e/icon-splash/splash-*.png     frames from a cold start (splash, then landing screen)
#   build/e2e/icon-splash/report.txt       pass/fail checks
#
# Usage: scripts/e2e-icon-splash.sh [device-serial]
# Needs: adb, ffmpeg, a built app/build/outputs/apk/debug/app-debug.apk
set -euo pipefail

SERIAL="${1:-${ANDROID_SERIAL:-}}"
ADB=(adb)
[[ -n "$SERIAL" ]] && ADB=(adb -s "$SERIAL")
PKG=com.theseuntaylor.hexo
APK=app/build/outputs/apk/debug/app-debug.apk
OUT=build/e2e/icon-splash
GROUND_HEX="0F2A33"

rm -rf "$OUT" && mkdir -p "$OUT"
"${ADB[@]}" install -r "$APK" >/dev/null

# 1. Icon: the app info screen shows the icon the launcher resolves.
"${ADB[@]}" shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d "package:$PKG" >/dev/null
sleep 2
"${ADB[@]}" exec-out screencap -p > "$OUT/app-info.png"

# 2. Splash: record a cold start and keep a few frames.
"${ADB[@]}" shell am force-stop "$PKG"
"${ADB[@]}" shell input keyevent KEYCODE_HOME
sleep 1
"${ADB[@]}" shell screenrecord --time-limit 4 /sdcard/hexo-start.mp4 &
REC=$!
sleep 0.5
"${ADB[@]}" shell am start -W -n "$PKG/.MainActivity" > "$OUT/am-start.txt"
wait "$REC" || true
"${ADB[@]}" pull /sdcard/hexo-start.mp4 "$OUT/start.mp4" >/dev/null
"${ADB[@]}" shell rm /sdcard/hexo-start.mp4
ffmpeg -loglevel error -i "$OUT/start.mp4" -vf fps=8 "$OUT/splash-%02d.png"

# Pixel near the top-left of a frame, as RRGGBB.
pixel() { ffmpeg -loglevel error -i "$1" -vf "crop=1:1:10:200" -f rawvideo -pix_fmt rgb24 - | xxd -p -u; }
# True when two RRGGBB colours differ by at most 6 per channel (video compression shifts colours).
near() {
  local i a b
  for i in 0 2 4; do
    a=$((16#${1:i:2})); b=$((16#${2:i:2}))
    (( a - b > 6 || b - a > 6 )) && return 1
  done
  return 0
}

# 3. Checks.
{
  echo "device: $("${ADB[@]}" shell getprop ro.product.model) (API $("${ADB[@]}" shell getprop ro.build.version.sdk))"
  grep -E "Status|TotalTime" "$OUT/am-start.txt"
  # A splash frame is one whose top-left pixel is the ground colour.
  SPLASH_FRAMES=0
  for f in "$OUT"/splash-*.png; do
    near "$(pixel "$f")" "$GROUND_HEX" && SPLASH_FRAMES=$((SPLASH_FRAMES + 1))
  done
  echo "splash frames with ground #$GROUND_HEX: $SPLASH_FRAMES"
  LAST=$(ls "$OUT"/splash-*.png | tail -1)
  last_px=$(pixel "$LAST")
  echo "last frame pixel: #$last_px (landing screen expected, not ground)"
  if [[ $SPLASH_FRAMES -gt 0 ]] && ! near "$last_px" "$GROUND_HEX"; then echo "RESULT: PASS"; else echo "RESULT: FAIL"; fi
} | tee "$OUT/report.txt"
