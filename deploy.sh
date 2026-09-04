#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "🚀 1. Building Latest Web Code..."
echo "=========================================="

"$SCRIPT_DIR/build_web.sh"

echo "=========================================="
echo "🌐 2. Deploying to Firebase Hosting & Rules..."
echo "=========================================="

cd "$SCRIPT_DIR"
npx -y firebase-tools@latest deploy --only hosting,firestore:rules --project onedaydelivery-market

echo "=========================================="
echo "🎉 DEPLOYMENT COMPLETE! LIVE AT https://1dd.web.app"
echo "=========================================="
