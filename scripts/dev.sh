#!/usr/bin/env bash
set -e

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
ADB="$HOME/Library/Android/sdk/platform-tools/adb"
EMU="$HOME/Library/Android/sdk/emulator/emulator"
AVD="Medium_Tablet"
PKG="com.learnlab.android.debug"

if ! "$ADB" devices | grep -q emulator; then
  echo "Starting emulator $AVD..."
  "$EMU" -avd "$AVD" >/dev/null 2>&1 &
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed | tr -d '\r')" = "1" ]; do
    sleep 2
  done
  echo "Emulator booted."
fi

./gradlew :app:installDebug
"$ADB" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null
echo "LearnLab launched."
