#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FLUTTER_DIR="$SCRIPT_DIR/flutter_app"
EXPORTS_WEB="$SCRIPT_DIR/exports/web"

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
echo "🚀 Building Flutter Web Application..."
echo "=========================================="

cd "$FLUTTER_DIR"
"$FLUTTER_CMD" build web --release

echo "=========================================="
echo "📦 Syncing Web Build to exports/web..."
echo "=========================================="

mkdir -p "$EXPORTS_WEB"
# Clean exports/web and copy new files
rm -rf "$EXPORTS_WEB"/*
cp -r "$FLUTTER_DIR/build/web"/* "$EXPORTS_WEB/"

# Generate version metadata with build timestamp
BUILD_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
cat << VERSION_EOF > "$EXPORTS_WEB/version.json"
{
  "app_name": "onedaydelivery",
  "version": "1.0.0",
  "build_number": "$(date +%s)",
  "build_time": "$BUILD_TIMESTAMP"
}
VERSION_EOF
cp "$EXPORTS_WEB/version.json" "$FLUTTER_DIR/build/web/version.json"

echo "✅ Web build & export completed successfully at $BUILD_TIMESTAMP!"
