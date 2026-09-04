# 1DD Marketplace - Unified Flutter Cross-Platform Distribution

This directory contains the production builds generated from the single unified Flutter codebase (`flutter_app/`).

---

## 📦 Exported Artifacts

### 1. Web Application
- **Distribution Directory**: `exports/web/`
- **Live Hosted URL**: [https://1dd.web.app](https://1dd.web.app)
- **One-Command Build & Deploy**:
  ```bash
  ./deploy.sh
  # or
  npm run deploy
  ```
- **Build Web Only**:
  ```bash
  ./build_web.sh
  # or
  npm run build:web
  ```

### 2. Android APK
- **File**: `exports/android/1DD-Marketplace.apk`
- **Build Mode**: Release (`assembleRelease`)
- **Compatibility**: Android 5.0+ (API 21 to 35)
- **Application ID**: `com.aistudio.localmarket.nxwzly`
- **Build APK Command**:
  ```bash
  ./build_apk.sh
  # or
  npm run build:apk
  ```

### 3. Build All Platforms
- **Command**:
  ```bash
  ./build_all.sh
  # or
  npm run build
  ```

---

## 🚀 Architecture Summary

- **Single Codebase**: All buyer storefront & seller dashboard interfaces run from `flutter_app/`.
- **Cloud Backend**: Live Google Cloud Firestore (`onedaydelivery-market` project).
- **Buyer Interface**:
  - Open browsing without login requirement.
  - Search & category filters.
  - Dynamic Haversine distance & ETA calculation based on delivery landmark.
  - Cash on Delivery (COD) 1-Day order placement.
  - Live OTP order status tracking dialog.
- **Seller Interface**:
  - Protected by Firebase Auth.
  - Restricted whitelist: Only `angsudas62@gmail.com` can access the seller dashboard.
  - Live inventory manager (add, quick stock edit, delete).
  - Live real-time incoming order tracker with state advancement (`Preparing` -> `Out for Delivery` -> `Delivered`).
