# 1DD Marketplace (Rangachakua Store) 🚀

[![Flutter](https://img.shields.io/badge/Flutter-3.47.0-02569B?style=for-the-badge&logo=flutter&logoColor=white)](https://flutter.dev)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%26%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Web Live](https://img.shields.io/badge/Live%20Web-1dd.web.app-15803D?style=for-the-badge&logo=google-chrome&logoColor=white)](https://1dd.web.app)
[![Android](https://img.shields.io/badge/Android%20APK-Release%20Ready-3DDC84?style=for-the-badge&logo=android&logoColor=white)](exports/android/1DD-Marketplace.apk)

A modern, full-stack, unified Flutter cross-platform e-commerce and local delivery marketplace designed for **Rangachakua Store (1DD)**. Offers frictionless instant shopping for buyers and a real-time reactive management dashboard for store owners.

---

## 🌐 Live Web Application

- **Production URL**: [https://1dd.web.app](https://1dd.web.app)
- **Status**: Live & deployed on Google Firebase Hosting with automated cache-busting.

---

## 📱 Features

### 🛍️ Buyer Storefront (Instant Access)
- **Zero Friction**: Open access without mandatory login for customers.
- **Dynamic Category Filtering**: Vegetables, Fruits, Groceries, Bakery, Beverages, and Dairy.
- **Real-time Haversine Distance & ETA**: Dynamically calculates delivery distance (in km) and estimated delivery time from the Rangachakua Store House to the customer's delivery landmark.
- **Cart & Cash on Delivery (COD)**: Add to cart, adjust quantities, review order summary, and place 1-Day COD orders.
- **OTP Order Tracking**: Generates secure 4-digit OTP codes and provides live modal tracking for customer order status (`Placed` ➔ `Preparing` ➔ `Out for Delivery` ➔ `Delivered`).

### 🏪 Store House Owner Dashboard
- **Protected Access**: Firebase Authentication with strict email whitelist access (`angsudas62@gmail.com`). Supports both **Sign in with Google** and **Email & Password**.
- **Real-Time Inventory Management**: Live cloud sync with Cloud Firestore. Add new products, update prices, quick-edit stock numbers, or remove items.
- **Live Order Feed**: Real-time incoming customer orders with customer contact details, items breakdown, Haversine distance, and 1-tap phone dialer.
- **Workflow State Management**: Advance orders through `Accept & Prepare` ➔ `Out for Delivery` ➔ `Mark Delivered`.

---

## 🏗️ Project Architecture

```
LUCKY/
├── flutter_app/                   # Unified Flutter application codebase
│   ├── lib/
│   │   ├── config/                # Firebase options & store location constants
│   │   ├── models/                # Product, Order, & Cart models
│   │   ├── providers/             # Provider state management (CartProvider)
│   │   ├── screens/               # Buyer Home, Seller Dashboard, Order Tracker
│   │   ├── services/              # Firebase Auth, Firestore, Haversine Engine
│   │   └── main.dart              # Application entrypoint
│   ├── web/                       # Web PWA assets, manifest, cache-busting loader
│   ├── android/                   # Native Android wrapper & Gradle configuration
│   └── pubspec.yaml               # Dependencies and metadata
├── exports/                       # Production distribution artifacts
│   ├── web/                       # Compiled production web bundle
│   └── android/                   # 1DD-Marketplace.apk (Release build)
├── firebase.json                  # Firebase Hosting & Firestore rules configuration
├── firestore.rules                # Cloud Firestore security rules
├── build_web.sh                   # Script to build & sync web release
├── build_apk.sh                   # Script to build & sync Android APK
├── build_all.sh                   # Script to build all platforms
├── deploy.sh                      # One-command build & Firebase deployment
└── package.json                   # NPM shortcuts for build & deploy workflows
```

---

## ⚡ Quick Start & Commands

### Prerequisites
- [Flutter SDK](https://flutter.dev) (v3.13.0+ / 3.47.0+)
- [Node.js](https://nodejs.org) (v18+) & `firebase-tools`

### 1. Build and Deploy Web Live (One Command)
```bash
./deploy.sh
# or using npm
npm run deploy
```

### 2. Build Web Application Only
```bash
./build_web.sh
# or using npm
npm run build:web
```

### 3. Build Android Release APK
```bash
./build_apk.sh
# or using npm
npm run build:apk
```

### 4. Build All Platforms
```bash
./build_all.sh
# or using npm
npm run build
```

---

## 🔒 Firebase Configuration & Setup

### Cloud Firestore Rules
Firestore rules are managed in [`firestore.rules`](firestore.rules) and are deployed automatically with `./deploy.sh`.

### Firebase Authentication Setup
1. Go to **Firebase Console** ➔ **Authentication** ➔ **Sign-in method**.
2. Enable **Email/Password** and **Google** sign-in providers.
3. In **Settings** ➔ **Authorized domains**, ensure the following domains are added:
   - `1dd.web.app`
   - `onedaydelivery-market.firebaseapp.com`
   - `localhost`

---

## 📄 License
This project is proprietary and maintained for **Rangachakua Store (1DD Marketplace)**.
