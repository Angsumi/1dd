#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "🌟 Starting Full Cross-Platform Build..."
echo "=========================================="

"$SCRIPT_DIR/build_web.sh"
"$SCRIPT_DIR/build_apk.sh"

echo "=========================================="
echo "🎉 ALL BUILDS COMPLETED & SYNCED TO EXPORTS!"
echo "=========================================="
