#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FLUTTER_DIR="$SCRIPT_DIR/flutter_app"
EXPORTS_APK="$SCRIPT_DIR/exports/android"

# Detect Flutter
if command -v flutter &> /dev/null; then
  FLUTTER_CMD="flutter"
elif [ -f "/home/angsuman/flutter_sdk/bin/flutter" ]; then
  FLUTTER_CMD="/home/angsuman/flutter_sdk/bin/flutter"
elif [ -f "$HOME/flutter_sdk/bin/flutter" ]; then
  FLUTTER_CMD="$HOME/flutter_sdk/bin/flutter"
else
  echo "Error: Flutter SDK not found!"
  exit 1
fi

echo "=========================================="
echo "🚀 Building Android Release APK..."
echo "=========================================="

cd "$FLUTTER_DIR"
"$FLUTTER_CMD" build apk --release

echo "=========================================="
echo "📦 Syncing APK to exports/android..."
echo "=========================================="

mkdir -p "$EXPORTS_APK"
APK_SOURCE="$FLUTTER_DIR/build/app/outputs/flutter-apk/app-release.apk"

if [ -f "$APK_SOURCE" ]; then
  cp -f "$APK_SOURCE" "$EXPORTS_APK/1DD-Marketplace.apk"
  echo "✅ Android APK exported successfully to $EXPORTS_APK/1DD-Marketplace.apk ($(du -h "$EXPORTS_APK/1DD-Marketplace.apk" | cut -f1))"
else
  echo "❌ Error: APK output file not found at $APK_SOURCE"
  exit 1
fi
