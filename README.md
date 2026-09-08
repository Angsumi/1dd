# 1DD Marketplace (Rangachakua Store) 🚀

[![GitHub Repo](https://img.shields.io/badge/GitHub-Angsumi%2F1dd-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Angsumi/1dd)
[![Live Web](https://img.shields.io/badge/Live%20Store-1dd.web.app-15803D?style=for-the-badge&logo=google-chrome&logoColor=white)](https://1dd.web.app)
[![Flutter](https://img.shields.io/badge/Flutter-3.47.0-02569B?style=for-the-badge&logo=flutter&logoColor=white)](https://flutter.dev)
[![Firebase](https://img.shields.io/badge/Firebase-Hosting%20%26%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://console.firebase.google.com/project/onedaydelivery-market/overview)
[![WhatsApp Catalog](https://img.shields.io/badge/WhatsApp%20Catalog-Meta%20Feed-25D366?style=for-the-badge&logo=whatsapp&logoColor=white)](https://1dd.web.app/whatsapp-catalog.csv)
[![Android APK](https://img.shields.io/badge/Android%20APK-Release%20Ready-3DDC84?style=for-the-badge&logo=android&logoColor=white)](exports/android/1DD-Marketplace.apk)

A complete, full-stack unified e-commerce platform and 1-day hyper-local delivery system designed for **Rangachakua Store (1DD)**. Integrates a Flutter storefront, a real-time reactive merchant dashboard, automated Firebase global CDN image hosting, and synchronized Meta WhatsApp Business catalog feeds.

---

## 🔗 Quick Links & Central Directory

### 🌐 Storefront & Applications
| Platform / Resource | Link | Description |
| :--- | :--- | :--- |
| **Official Online Store** | [https://1dd.web.app](https://1dd.web.app) | Public customer storefront with instant COD ordering |
| **Alternative App Domain** | [onedaydelivery-market.firebaseapp.com](https://onedaydelivery-market.firebaseapp.com) | Secondary Firebase hosting domain |
| **GitHub Repository** | [https://github.com/Angsumi/1dd](https://github.com/Angsumi/1dd) | Main source code repository |
| **Android APK** | [`exports/android/1DD-Marketplace.apk`](exports/android/1DD-Marketplace.apk) | Production release build for Android devices |
| **Visual Catalog Review** | [https://1dd.web.app/products_review.html](https://1dd.web.app/products_review.html) | Live interactive review dashboard for all 125 products |

---

### 📦 Meta & WhatsApp Business Catalog Feeds
| Feed / Tool | URL / Identifier | Purpose |
| :--- | :--- | :--- |
| **Live Scheduled Data Feed** | `https://1dd.web.app/whatsapp-catalog.csv` | Primary URL for Meta Commerce Manager auto-sync |
| **Direct Meta Feed (Mirror)** | `https://1dd.web.app/final.csv` | Secondary live mirror feed |
| **Meta Catalog ID** | `1066754452729858` | Commerce Manager Catalog Identifier |
| **Meta Data Source ID** | `1080127838321211` | Data Feed Source Identifier |
| **Commerce Manager Direct URL**| [Meta Commerce Manager](https://business.facebook.com/commerce/catalogs/1066754452729858/) | Manage products, collections, and channel links |
| **Data Source Settings** | [Commerce Manager Data Source](https://business.facebook.com/commerce/catalogs/1066754452729858/data_sources/1080127838321211/) | Feed schedule & upload history |

---

### ☁️ Cloud & Infrastructure
| Service | Console / Resource | Details |
| :--- | :--- | :--- |
| **Firebase Project** | `onedaydelivery-market` | Google Cloud / Firebase project |
| **Firebase Console** | [Firebase Console Overview](https://console.firebase.google.com/project/onedaydelivery-market/overview) | Hosting, Firestore, Auth, Storage monitoring |
| **Cloud Firestore** | Active | Real-time database for inventory & customer orders |
| **Firebase Authentication** | Whitelist: `angsudas62@gmail.com` | Google Sign-in & Email/Password authentication |
| **Product Images CDN** | `https://1dd.web.app/images/products/` | Global CDN delivering all 125 packaged item photos |

---

## 📁 Data Sources & Inventory Files

All grocery product data is standardized in the `1DD/` workspace directory:

- **[`final.csv`](file:///home/angsuman/extra_spac/1DD/final.csv)**:
  - **Type**: Clean Meta / WhatsApp Catalog Feed (without internal buying cost).
  - **Schema**: `id`, `title`, `description`, `availability`, `condition`, `price`, `link`, `image_link`, `brand`, `google_product_category`, `fb_product_category`.
  - **Encoding**: UTF-8 with BOM for native Assamese script (`অসমীয়া লিপি`) support in Excel & Meta.
  - **Sorting**: Alphabetical (A to Z) by `title`.
  - **Title Convention**: `"Category | Brand | Amount"` (e.g. `Salt | Z + | 1 kg`, `Pulses | Arhar (ৰহৰ দাইল) | 1 kg`).

- **[`final_with_buying_cost.csv`](file:///home/angsuman/extra_spac/1DD/final_with_buying_cost.csv)**:
  - **Type**: Complete Internal Business & Inventory Master (with cost & margins).
  - **Schema**: `id`, `title`, `buying_cost`, `price`, `profit_margin`, `availability`, `condition`, `brand`, `category`, `pack_size`, `link`, `image_link`, `google_product_category`, `description`.

- **[`products.txt`](file:///home/angsuman/extra_spac/1DD/products.txt)**:
  - **Type**: Streamlined 3-field text format for rapid manual entry into WhatsApp Web:
    ```text
    Name: <Category | Brand | Amount>
    Price: <MRP in ₹>
    Country of origin: India
    ```

- **[`product_images/`](file:///home/angsuman/extra_spac/1DD/product_images)**:
  - **Type**: Local repository of all 125 high-resolution packaged product images, named with exact product titles and synced live to Firebase Hosting.

---

## 📱 Application Capabilities

### 🛍️ Buyer Storefront (`https://1dd.web.app`)
- **Frictionless Shopping**: Direct customer access without mandatory login.
- **Dynamic Category Filtering**: Vegetables, Fruits, Groceries, Dals & Pulses, Spices, Dairy, Soaps, etc.
- **Real-Time Distance & ETA**: Uses the Haversine formula to compute delivery distance (km) and estimated delivery time from Rangachakua Store House to customer landmarks.
- **Cart & Cash on Delivery (COD)**: Quick order placement with 4-digit order confirmation OTP tracking.
- **Order Status Modal**: Live updates (`Placed` ➔ `Preparing` ➔ `Out for Delivery` ➔ `Delivered`).

### 🏪 Store Owner Dashboard
- **Protected Access**: Firebase Authentication with strict email whitelist access (`angsudas62@gmail.com`).
- **Live Inventory Control**: Instant stock edits, price adjustments, and item removals synced via Cloud Firestore.
- **Incoming Order Feed**: Real-time customer notifications, address details, item breakdown, and 1-tap dialer.

---

## 🛠️ Build & Deployment Commands

Run these commands from the `LUCKY/` project directory:

### 1. Full Deploy (Build + Firebase Live Sync)
```bash
./deploy.sh
# or via npm
npm run deploy
```

### 2. Deploy Web Only
```bash
./build_web.sh
firebase deploy --only hosting
```

### 3. Build Android APK
```bash
./build_apk.sh
```

---

## 📄 License & Ownership
Proprietary platform built and maintained for **Rangachakua Store (1DD Marketplace)**.
