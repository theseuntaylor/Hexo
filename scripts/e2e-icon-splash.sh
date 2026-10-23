#!/usr/bin/env bash
# E2E check for the launcher icon, the launch splash screen and the xo theme.
#
# Installs the debug APK on a device, then captures:
#   build/e2e/icon-splash/app-info.png     the app's icon as the system shows it
#   build/e2e/icon-splash/splash-*.png     frames from a cold start (splash, then landing screen)
#   build/e2e/icon-splash/screen-*.png     main screens in the xo theme
#   build/e2e/icon-splash/contact.png      splash, icon and main screens side by side
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
HEX_HEX="18414F"  # hexagon fill; the screen centre is inside it only on the splash

rm -rf "$OUT" && mkdir -p "$OUT"
"${ADB[@]}" install -r "$APK" >/dev/null

# 1. Icon: the app info screen shows the icon the launcher resolves.
"${ADB[@]}" shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d "package:$PKG" >/dev/null
sleep 2
"${ADB[@]}" exec-out screencap -p > "$OUT/app-info.png"

# Centre of the first UI node whose text is exactly $1, as "x y".
node_center() {
  "${ADB[@]}" shell uiautomator dump /sdcard/ui.xml >/dev/null
  "${ADB[@]}" exec-out cat /sdcard/ui.xml | python3 -c '
import re, sys
want = sys.argv[1]
for n in re.findall(r"<node [^>]*>", sys.stdin.read()):
    t = re.search(r"text=\"([^\"]*)\"", n).group(1)
    if t == want:
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", re.search(r"bounds=\"([^\"]*)\"", n).group(1)))
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
' "$1"
}
tap_text() {
  local xy
  xy=$(node_center "$1")
  [[ -z "$xy" ]] && { echo "missing on screen: $1" >&2; return 1; }
  "${ADB[@]}" shell input tap $xy
  sleep 1.5
}
# Centres of the 9 board cells (square clickable nodes), one "x y" per line.
board_cells() {
  "${ADB[@]}" shell uiautomator dump /sdcard/ui.xml >/dev/null
  "${ADB[@]}" exec-out cat /sdcard/ui.xml | python3 -c '
import re, sys
for n in re.findall(r"<node [^>]*>", sys.stdin.read()):
    if "clickable=\"true\"" not in n: continue
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", re.search(r"bounds=\"([^\"]*)\"", n).group(1)))
    w, h = x2 - x1, y2 - y1
    if w > 200 and abs(w - h) < 8: print((x1 + x2) // 2, (y1 + y2) // 2)
'
}
shot() { "${ADB[@]}" exec-out screencap -p > "$OUT/screen-$1.png"; }

# 2. Splash: record a cold start until the landing screen shows, and keep frames.
"${ADB[@]}" shell am force-stop "$PKG"
"${ADB[@]}" shell input keyevent KEYCODE_HOME
sleep 1
"${ADB[@]}" shell screenrecord --time-limit 30 /sdcard/hexo-start.mp4 &
REC=$!
sleep 0.5
"${ADB[@]}" shell am start -W -n "$PKG/.MainActivity" > "$OUT/am-start.txt" 2>&1
for _ in $(seq 1 30); do
  [[ -n "$(node_center "HEXO")" ]] && break
  sleep 1
done
sleep 1
"${ADB[@]}" shell pkill -INT screenrecord || true
wait "$REC" || true
"${ADB[@]}" pull /sdcard/hexo-start.mp4 "$OUT/start.mp4" >/dev/null
"${ADB[@]}" shell rm /sdcard/hexo-start.mp4
ffmpeg -loglevel error -i "$OUT/start.mp4" -vf fps=8 "$OUT/splash-%02d.png"

# Pixel near the top-left of a frame, as RRGGBB.
pixel() { ffmpeg -loglevel error -i "$1" -vf "crop=1:1:10:200" -f rawvideo -pix_fmt rgb24 - | xxd -p -u; }
# Pixel at the centre of a frame, as RRGGBB.
center_pixel() { ffmpeg -loglevel error -i "$1" -vf "crop=1:1:iw/2:ih/2" -f rawvideo -pix_fmt rgb24 - | xxd -p -u; }
# True when two RRGGBB colours differ by at most 6 per channel (video compression shifts colours).
near() {
  local i a b
  for i in 0 2 4; do
    a=$((16#${1:i:2})); b=$((16#${2:i:2}))
    (( a - b > 6 || b - a > 6 )) && return 1
  done
  return 0
}


# 3. Main screens in the theme.
shot landing
tap_text "Play vs Computer"; shot computer-setup
tap_text "Easy"
CELLS=$(board_cells)
"${ADB[@]}" shell input tap $(sed -n 5p <<< "$CELLS"); sleep 2
"${ADB[@]}" shell input tap $(sed -n 1p <<< "$CELLS"); sleep 2
shot computer-game
tap_text "Back to Menu"
tap_text "Create A Room"; shot create-room
SPLASH_SHOT=""
for f in "$OUT"/splash-*.png; do
  near "$(center_pixel "$f")" "$HEX_HEX" && SPLASH_SHOT="$f"
done
SPLASH_SHOT="${SPLASH_SHOT:-$(ls "$OUT"/splash-*.png | head -1)}"
ffmpeg -loglevel error -y -i "$SPLASH_SHOT" -i "$OUT/app-info.png" -i "$OUT/screen-landing.png" \
  -i "$OUT/screen-computer-setup.png" -i "$OUT/screen-computer-game.png" -i "$OUT/screen-create-room.png" \
  -filter_complex "[0]scale=360:-1[a];[1]scale=360:-1[b];[2]scale=360:-1[c];[3]scale=360:-1[d];[4]scale=360:-1[e];[5]scale=360:-1[f];[a][b][c][d][e][f]hstack=6" \
  "$OUT/contact.png"

# 4. Checks.
{
  echo "device: $("${ADB[@]}" shell getprop ro.product.model) (API $("${ADB[@]}" shell getprop ro.build.version.sdk))"
  grep -E "Status|TotalTime" "$OUT/am-start.txt"
  # A splash frame has the ground at the top-left and the hexagon at the centre.
  SPLASH_FRAMES=0
  for f in "$OUT"/splash-*.png; do
    near "$(pixel "$f")" "$GROUND_HEX" && near "$(center_pixel "$f")" "$HEX_HEX" && SPLASH_FRAMES=$((SPLASH_FRAMES + 1))
  done
  echo "splash frames (ground #$GROUND_HEX, hexagon #$HEX_HEX at centre): $SPLASH_FRAMES"
  LAST=$(ls "$OUT"/splash-*.png | tail -1)
  last_center=$(center_pixel "$LAST")
  echo "last frame centre pixel: #$last_center (landing screen expected, not the hexagon)"
  # The xo theme paints every screen with the same ground as the splash.
  THEMED=0
  for f in "$OUT"/screen-*.png; do
    px=$(pixel "$f")
    echo "$(basename "$f") background: #$px"
    near "$px" "$GROUND_HEX" && THEMED=$((THEMED + 1))
  done
  SCREENS=$(ls "$OUT"/screen-*.png | wc -l | tr -d ' ')
  echo "screens with ground #$GROUND_HEX: $THEMED of $SCREENS"
  if [[ $SPLASH_FRAMES -gt 0 && $THEMED -eq $SCREENS && $SCREENS -eq 4 ]] && ! near "$last_center" "$HEX_HEX"; then echo "RESULT: PASS"; else echo "RESULT: FAIL"; fi
} | tee "$OUT/report.txt"
