import csv
import json
import urllib.parse

with open('final_with_buying_cost.csv', 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f)
    raw_products = list(reader)

products = []
for idx, r in enumerate(raw_products):
    buying_str = r.get('buying_cost', '0').replace(' INR', '').strip()
    price_str = r.get('price', '0').replace(' INR', '').strip()
    profit_str = r.get('profit_margin', '0').replace(' INR', '').strip()
    
    buying_val = float(buying_str) if buying_str else 0.0
    price_val = float(price_str) if price_str else 0.0
    profit_val = float(profit_str) if profit_str else round(price_val - buying_val, 2)
    margin_pct = round((profit_val / price_val * 100), 1) if price_val > 0 else 0.0
    markup_pct = round((profit_val / buying_val * 100), 1) if buying_val > 0 else 0.0
    
    img_url = r.get('image_link', '')
    img_file = urllib.parse.unquote(img_url.split('/')[-1])
    local_path = f"product_images/{urllib.parse.quote(img_file)}"

    products.append({
        "id": r.get("id", f"prod-{idx+1}"),
        "title": r.get("title", ""),
        "buying_cost": buying_val,
        "price": price_val,
        "profit": profit_val,
        "margin_pct": margin_pct,
        "markup_pct": markup_pct,
        "availability": r.get("availability", "in stock"),
        "condition": r.get("condition", "new"),
        "brand": r.get("brand", ""),
        "category": r.get("category", ""),
        "pack_size": r.get("pack_size", ""),
        "link": r.get("link", ""),
        "image_link": img_url,
        "local_image": local_path,
        "description": r.get("description", "")
    })

products_json = json.dumps(products, indent=2)

template = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>1DD Grocery - Product & Pricing Master Catalog</title>
  <meta name="description" content="1DD Grocery catalog with small row thumbnails, selling prices, buying costs, profit margins, brand and category filters." />
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0f172a;
      --surface: #1e293b;
      --surface-card: #151d30;
      --surface-hover: #27354f;
      --surface-active: #334155;
      --border: #334155;
      --border-subtle: #1e293b;
      --text-main: #f8fafc;
      --text-muted: #94a3b8;
      --text-dim: #64748b;
      --primary: #38bdf8;
      --primary-hover: #0284c7;
      --primary-glow: rgba(56, 189, 248, 0.18);
      --accent: #818cf8;
      --success: #10b981;
      --success-bg: rgba(16, 185, 129, 0.15);
      --warning: #f59e0b;
      --warning-bg: rgba(245, 158, 11, 0.15);
      --danger: #ef4444;
      --danger-bg: rgba(239, 68, 68, 0.15);
      --radius-sm: 6px;
      --radius-md: 10px;
      --radius-lg: 14px;
      --font-mono: 'JetBrains Mono', monospace;
      --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }

    [data-theme="light"] {
      --bg: #f8fafc;
      --surface: #ffffff;
      --surface-card: #f1f5f9;
      --surface-hover: #f1f5f9;
      --surface-active: #e2e8f0;
      --border: #e2e8f0;
      --border-subtle: #cbd5e1;
      --text-main: #0f172a;
      --text-muted: #475569;
      --text-dim: #94a3b8;
      --primary: #0284c7;
      --primary-hover: #0369a1;
      --primary-glow: rgba(2, 132, 199, 0.12);
      --accent: #4f46e5;
      --success: #059669;
      --success-bg: #d1fae5;
      --warning: #d97706;
      --warning-bg: #fef3c7;
      --danger: #dc2626;
      --danger-bg: #fee2e2;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }

    body {
      font-family: var(--font-sans);
      background-color: var(--bg);
      color: var(--text-main);
      min-height: 100vh;
      line-height: 1.5;
      -webkit-font-smoothing: antialiased;
    }

    .app-container {
      max-width: 1560px;
      margin: 0 auto;
      padding: 24px 20px 80px;
    }

    /* Header Section */
    .header {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 24px;
    }

    .top-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }

    .branding {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .logo-badge {
      background: linear-gradient(135deg, #38bdf8, #6366f1);
      color: #fff;
      font-weight: 800;
      font-size: 19px;
      padding: 6px 14px;
      border-radius: var(--radius-md);
      box-shadow: 0 4px 14px rgba(56, 189, 248, 0.3);
      letter-spacing: 0.5px;
    }

    .header-title-group h1 {
      font-size: 24px;
      font-weight: 800;
      letter-spacing: -0.5px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .header-title-group p {
      font-size: 13.5px;
      color: var(--text-muted);
      margin-top: 2px;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .action-btn {
      background: var(--surface);
      border: 1px solid var(--border);
      color: var(--text-main);
      padding: 8px 14px;
      border-radius: var(--radius-sm);
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s ease;
    }

    .action-btn:hover {
      background: var(--surface-hover);
      border-color: var(--primary);
    }

    /* Stats Ribbon */
    .stats-ribbon {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 14px;
    }

    .stat-card {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 14px 16px;
      border-radius: var(--radius-md);
      display: flex;
      flex-direction: column;
      gap: 4px;
      transition: border-color 0.2s;
    }

    .stat-card:hover {
      border-color: var(--primary);
    }

    .stat-label {
      font-size: 11.5px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--text-muted);
    }

    .stat-value {
      font-size: 22px;
      font-weight: 800;
      font-family: var(--font-mono);
      color: var(--text-main);
    }

    .stat-meta {
      font-size: 11px;
      color: var(--text-dim);
    }

    /* Controls & Filter Toolbar */
    .toolbar-panel {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
      padding: 16px;
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .toolbar-row {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
      justify-content: space-between;
    }

    .search-wrapper {
      position: relative;
      flex: 1;
      min-width: 260px;
    }

    .search-wrapper svg {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-dim);
      pointer-events: none;
    }

    .search-input {
      width: 100%;
      background: var(--surface-card);
      border: 1px solid var(--border);
      color: var(--text-main);
      padding: 9px 12px 9px 38px;
      border-radius: var(--radius-sm);
      font-size: 14px;
      outline: none;
      transition: all 0.2s;
    }

    .search-input:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px var(--primary-glow);
    }

    .filter-group {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
    }

    .filter-select {
      background: var(--surface-card);
      border: 1px solid var(--border);
      color: var(--text-main);
      padding: 8px 12px;
      border-radius: var(--radius-sm);
      font-size: 13px;
      font-weight: 500;
      outline: none;
      cursor: pointer;
      min-width: 150px;
      max-width: 220px;
    }

    .filter-select:focus {
      border-color: var(--primary);
    }

    .segmented-control {
      display: flex;
      background: var(--surface-card);
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      padding: 2px;
      gap: 2px;
    }

    .segment-btn {
      background: transparent;
      border: none;
      color: var(--text-muted);
      padding: 6px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s;
    }

    .segment-btn.active {
      background: var(--primary);
      color: #0b132b;
    }
    [data-theme="light"] .segment-btn.active {
      color: #fff;
    }

    /* Category Quick Filter Chips */
    .chips-scroll {
      display: flex;
      gap: 8px;
      overflow-x: auto;
      padding-bottom: 4px;
      scrollbar-width: thin;
    }

    .chips-scroll::-webkit-scrollbar {
      height: 4px;
    }

    .chips-scroll::-webkit-scrollbar-thumb {
      background: var(--border);
      border-radius: 4px;
    }

    .chip {
      background: var(--surface-card);
      border: 1px solid var(--border);
      color: var(--text-muted);
      font-size: 12px;
      font-weight: 500;
      padding: 4px 12px;
      border-radius: 20px;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.15s;
      user-select: none;
    }

    .chip:hover {
      color: var(--text-main);
      border-color: var(--primary);
    }

    .chip.active {
      background: var(--primary-glow);
      color: var(--primary);
      border-color: var(--primary);
      font-weight: 700;
    }

    /* Active Filter Summary Bar */
    .filter-status {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      color: var(--text-muted);
      margin-bottom: 12px;
      padding: 0 4px;
    }

    .reset-btn {
      color: var(--primary);
      background: none;
      border: none;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      text-decoration: underline;
    }

    /* Table Layout */
    .table-container {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
      overflow-x: auto;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      position: relative;
    }

    .product-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 13.5px;
      white-space: nowrap;
    }

    .product-table th {
      background: var(--surface-card);
      color: var(--text-muted);
      font-size: 11.5px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.6px;
      padding: 12px 14px;
      border-bottom: 2px solid var(--border);
      user-select: none;
      position: sticky;
      top: 0;
      z-index: 2;
    }

    .product-table th.sortable {
      cursor: pointer;
    }

    .product-table th.sortable:hover {
      color: var(--primary);
      background: var(--surface-hover);
    }

    .sort-icon {
      display: inline-block;
      margin-left: 4px;
      font-size: 10px;
      opacity: 0.4;
      transition: opacity 0.2s;
    }

    .product-table th.active-sort .sort-icon {
      opacity: 1;
      color: var(--primary);
    }

    .product-table tbody tr {
      border-bottom: 1px solid var(--border);
      transition: background-color 0.15s ease;
    }

    .product-table tbody tr:hover {
      background-color: var(--surface-hover);
    }

    .product-table td {
      padding: 10px 14px;
      vertical-align: middle;
    }

    /* Compact / Spacious Density */
    body.density-compact .product-table td {
      padding: 6px 10px;
      font-size: 12.5px;
    }
    body.density-compact .thumb-wrapper {
      width: 36px;
      height: 36px;
    }
    body.density-spacious .product-table td {
      padding: 14px 16px;
      font-size: 14px;
    }
    body.density-spacious .thumb-wrapper {
      width: 52px;
      height: 52px;
    }

    /* Small Images in Rows */
    .thumb-wrapper {
      position: relative;
      width: 44px;
      height: 44px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: #ffffff;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border);
      overflow: hidden;
      cursor: pointer;
      flex-shrink: 0;
      transition: transform 0.15s, border-color 0.15s, box-shadow 0.15s;
    }

    .thumb-wrapper:hover {
      transform: scale(1.15);
      border-color: var(--primary);
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      z-index: 5;
    }

    .thumb-img {
      max-width: 90%;
      max-height: 90%;
      object-fit: contain;
      display: block;
    }

    /* Product Title & Details */
    .prod-cell {
      display: flex;
      align-items: center;
      gap: 12px;
      white-space: normal;
      min-width: 250px;
      max-width: 360px;
    }

    .prod-title {
      font-weight: 600;
      color: var(--text-main);
      line-height: 1.35;
      font-size: 13.5px;
      cursor: pointer;
    }

    .prod-title:hover {
      color: var(--primary);
    }

    .pack-badge {
      display: inline-block;
      font-size: 11px;
      font-weight: 700;
      background: var(--surface-active);
      color: #cbd5e1;
      padding: 2px 7px;
      border-radius: 4px;
      margin-top: 3px;
    }
    [data-theme="light"] .pack-badge {
      color: #475569;
    }

    .brand-cell {
      font-weight: 600;
      color: var(--text-main);
    }

    .category-badge {
      font-size: 11.5px;
      font-weight: 500;
      background: var(--surface-card);
      border: 1px solid var(--border);
      color: var(--text-muted);
      padding: 3px 8px;
      border-radius: 6px;
      display: inline-block;
    }

    /* Numbers & Currency Columns */
    .num-cell {
      font-family: var(--font-mono);
      font-weight: 600;
    }

    .buying-cost {
      color: var(--text-muted);
    }

    .selling-price {
      color: var(--text-main);
      font-weight: 700;
    }

    .profit-cell {
      color: var(--success);
      font-weight: 700;
    }

    .margin-pill {
      font-family: var(--font-mono);
      font-size: 11.5px;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 12px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    .margin-high {
      background: var(--success-bg);
      color: var(--success);
      border: 1px solid rgba(16, 185, 129, 0.3);
    }

    .margin-mid {
      background: var(--warning-bg);
      color: var(--warning);
      border: 1px solid rgba(245, 158, 11, 0.3);
    }

    .margin-low {
      background: var(--danger-bg);
      color: var(--danger);
      border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .stock-badge {
      font-size: 11.5px;
      font-weight: 600;
      color: var(--success);
      display: inline-flex;
      align-items: center;
      gap: 5px;
    }

    .stock-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--success);
      box-shadow: 0 0 6px var(--success);
    }

    .link-icon-btn {
      color: var(--text-dim);
      padding: 5px;
      border-radius: 4px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: color 0.15s, background-color 0.15s;
      text-decoration: none;
    }

    .link-icon-btn:hover {
      color: var(--primary);
      background: var(--surface-active);
    }

    /* Modal for Image Preview & Details */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.8);
      backdrop-filter: blur(5px);
      z-index: 1000;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .modal-backdrop.open {
      display: flex;
    }

    .modal-box {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius-lg);
      max-width: 650px;
      width: 100%;
      box-shadow: 0 20px 40px rgba(0,0,0,0.5);
      overflow: hidden;
      animation: modalSlide 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes modalSlide {
      from { transform: translateY(20px) scale(0.97); opacity: 0; }
      to { transform: translateY(0) scale(1); opacity: 1; }
    }

    .modal-header {
      padding: 16px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid var(--border);
    }

    .modal-header h3 {
      font-size: 16px;
      font-weight: 700;
    }

    .modal-close {
      background: transparent;
      border: none;
      color: var(--text-muted);
      font-size: 24px;
      cursor: pointer;
      line-height: 1;
      padding: 4px;
    }

    .modal-close:hover {
      color: var(--text-main);
    }

    .modal-body {
      padding: 20px;
      display: grid;
      grid-template-columns: 240px 1fr;
      gap: 20px;
    }

    @media (max-width: 620px) {
      .modal-body {
        grid-template-columns: 1fr;
      }
    }

    .modal-img-wrap {
      background: #ffffff;
      border-radius: var(--radius-md);
      border: 1px solid var(--border);
      height: 240px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 12px;
    }

    .modal-img-wrap img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

    .modal-info {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .modal-info-row {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      border-bottom: 1px solid var(--border);
      font-size: 13px;
    }

    .modal-info-row .lbl {
      color: var(--text-muted);
    }

    .modal-info-row .val {
      font-weight: 600;
      font-family: var(--font-mono);
    }

    .modal-desc {
      font-size: 12px;
      color: var(--text-muted);
      line-height: 1.4;
      margin-top: 6px;
    }

    .modal-footer {
      padding: 12px 20px;
      background: var(--surface-card);
      border-top: 1px solid var(--border);
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }

    /* Empty state */
    .empty-state {
      padding: 60px 20px;
      text-align: center;
      color: var(--text-muted);
    }

    /* Print styles */
    @media print {
      body {
        background: #fff !important;
        color: #000 !important;
      }
      .toolbar-panel, .header-actions, .stats-ribbon, .filter-status {
        display: none !important;
      }
      .table-container {
        border: 1px solid #ccc !important;
        box-shadow: none !important;
      }
      .product-table th, .product-table td {
        border-color: #eee !important;
        color: #000 !important;
      }
    }
  </style>
</head>
<body>

<div class="app-container">

  <!-- Header -->
  <header class="header">
    <div class="top-bar">
      <div class="branding">
        <div class="logo-badge">1DD</div>
        <div class="header-title-group">
          <h1>Product & Pricing Master Catalog</h1>
          <p>Complete product inventory breakdown with buying cost, selling price, profit margins & small thumbnail rows</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="action-btn" id="themeToggle" title="Toggle Dark/Light Mode">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          <span id="themeText">Theme</span>
        </button>
        <button class="action-btn" id="exportCsvBtn" title="Download filtered data as CSV">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
          Export CSV
        </button>
        <button class="action-btn" onclick="window.print()" title="Print Catalog">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4H7v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z"></path></svg>
          Print
        </button>
      </div>
    </div>

    <!-- Stats Ribbon -->
    <div class="stats-ribbon">
      <div class="stat-card">
        <span class="stat-label">Total Products</span>
        <span class="stat-value" id="statTotalCount">128</span>
        <span class="stat-meta" id="statFilteredMeta">All catalog items</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Selling Value</span>
        <span class="stat-value" id="statTotalSelling">₹0</span>
        <span class="stat-meta">Sum of 1 unit per item</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Cost Value</span>
        <span class="stat-value" id="statTotalCost">₹0</span>
        <span class="stat-meta">Sum of buying cost</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Potential Profit</span>
        <span class="stat-value" id="statTotalProfit" style="color: var(--success);">₹0</span>
        <span class="stat-meta" id="statAvgMargin">Avg Margin: 0%</span>
      </div>
    </div>
  </header>

  <!-- Filter & Search Toolbar -->
  <section class="toolbar-panel">
    <div class="toolbar-row">
      <div class="search-wrapper">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"></circle><path d="M21 21l-4.35-4.35"></path></svg>
        <input type="text" id="searchInput" class="search-input" placeholder="Search by name, brand, category, pack size, ID..." autocomplete="off" />
      </div>

      <div class="filter-group">
        <select id="categoryFilter" class="filter-select">
          <option value="">All Categories (All)</option>
        </select>

        <select id="brandFilter" class="filter-select">
          <option value="">All Brands (All)</option>
        </select>

        <select id="marginFilter" class="filter-select">
          <option value="">All Margins</option>
          <option value="high">High Margin (&ge; 20%)</option>
          <option value="mid">Medium Margin (10% - 19%)</option>
          <option value="low">Low Margin (&lt; 10%)</option>
        </select>

        <div class="segmented-control" title="Table Row Density">
          <button class="segment-btn" data-density="compact">Compact</button>
          <button class="segment-btn active" data-density="normal">Normal</button>
          <button class="segment-btn" data-density="spacious">Spacious</button>
        </div>
      </div>
    </div>

    <!-- Category Chips -->
    <div class="chips-scroll" id="categoryChips">
      <button class="chip active" data-cat="">All Categories</button>
    </div>
  </section>

  <!-- Filter status bar -->
  <div class="filter-status">
    <span id="resultsCount">Showing 128 products</span>
    <button class="reset-btn" id="resetFiltersBtn" style="display: none;">Reset all filters</button>
  </div>

  <!-- Data Table Container -->
  <div class="table-container">
    <table class="product-table" id="productTable">
      <thead>
        <tr>
          <th style="width: 45px; text-align: center;">#</th>
          <th style="width: 60px; text-align: center;">Image</th>
          <th class="sortable" data-sort="title">Product Name <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="category">Category <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="brand">Brand <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="pack_size">Pack Size <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="buying_cost" style="text-align: right;">Buying Cost <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="price" style="text-align: right;">Selling Price <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="profit" style="text-align: right;">Profit (₹) <span class="sort-icon">&uarr;&darr;</span></th>
          <th class="sortable" data-sort="margin_pct" style="text-align: center;">Margin % <span class="sort-icon">&uarr;&darr;</span></th>
          <th style="text-align: center;">Status</th>
          <th style="text-align: center; width: 60px;">Link</th>
        </tr>
      </thead>
      <tbody id="tableBody">
        <!-- Rendered via JS -->
      </tbody>
    </table>
    <div id="emptyState" class="empty-state" style="display: none;">
      <svg width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24" style="margin-bottom: 12px; color: var(--text-dim);"><circle cx="11" cy="11" r="8"></circle><path d="M21 21l-4.35-4.35"></path><path d="M8 11h6"></path></svg>
      <h3>No products matched your filters</h3>
      <p style="margin-top: 6px;">Try adjusting your search terms or resetting the active category/brand filter.</p>
    </div>
  </div>

</div>

<!-- Product Preview Modal -->
<div class="modal-backdrop" id="productModal">
  <div class="modal-box">
    <div class="modal-header">
      <h3 id="modalTitle">Product Details</h3>
      <button class="modal-close" id="modalCloseBtn">&times;</button>
    </div>
    <div class="modal-body">
      <div class="modal-img-wrap">
        <img id="modalImg" src="" alt="Product Image" />
      </div>
      <div class="modal-info">
        <div class="modal-info-row">
          <span class="lbl">Product ID</span>
          <span class="val" id="modalId" style="font-size: 11px;"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Category</span>
          <span class="val" id="modalCategory"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Brand</span>
          <span class="val" id="modalBrand"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Pack Size</span>
          <span class="val" id="modalPack"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Buying Cost</span>
          <span class="val" id="modalCost" style="color: var(--text-muted);"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Selling Price</span>
          <span class="val" id="modalPrice" style="color: var(--primary);"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Profit per Unit</span>
          <span class="val" id="modalProfit" style="color: var(--success);"></span>
        </div>
        <div class="modal-info-row">
          <span class="lbl">Margin % (on MRP)</span>
          <span class="val" id="modalMargin"></span>
        </div>
        <p class="modal-desc" id="modalDesc"></p>
      </div>
    </div>
    <div class="modal-footer">
      <a href="#" target="_blank" class="action-btn" id="modalStoreLink" style="background: var(--primary); color: #0b132b; border: none; font-weight: 700;">
        Open in Store
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path></svg>
      </a>
    </div>
  </div>
</div>

<script>
  // Embedded dataset
  const PRODUCTS_DATA = __PRODUCTS_PLACEHOLDER__;

  let currentSort = { field: null, asc: true };
  let activeCategory = "";
  let activeBrand = "";
  let activeMargin = "";
  let searchQuery = "";

  // Elements
  const tableBody = document.getElementById("tableBody");
  const emptyState = document.getElementById("emptyState");
  const searchInput = document.getElementById("searchInput");
  const categoryFilter = document.getElementById("categoryFilter");
  const brandFilter = document.getElementById("brandFilter");
  const marginFilter = document.getElementById("marginFilter");
  const categoryChips = document.getElementById("categoryChips");
  const resultsCount = document.getElementById("resultsCount");
  const resetFiltersBtn = document.getElementById("resetFiltersBtn");
  const themeToggle = document.getElementById("themeToggle");
  const exportCsvBtn = document.getElementById("exportCsvBtn");

  // Stats elements
  const statTotalCount = document.getElementById("statTotalCount");
  const statFilteredMeta = document.getElementById("statFilteredMeta");
  const statTotalSelling = document.getElementById("statTotalSelling");
  const statTotalCost = document.getElementById("statTotalCost");
  const statTotalProfit = document.getElementById("statTotalProfit");
  const statAvgMargin = document.getElementById("statAvgMargin");

  // Modal elements
  const modal = document.getElementById("productModal");
  const modalCloseBtn = document.getElementById("modalCloseBtn");
  const modalTitle = document.getElementById("modalTitle");
  const modalImg = document.getElementById("modalImg");
  const modalId = document.getElementById("modalId");
  const modalCategory = document.getElementById("modalCategory");
  const modalBrand = document.getElementById("modalBrand");
  const modalPack = document.getElementById("modalPack");
  const modalCost = document.getElementById("modalCost");
  const modalPrice = document.getElementById("modalPrice");
  const modalProfit = document.getElementById("modalProfit");
  const modalMargin = document.getElementById("modalMargin");
  const modalDesc = document.getElementById("modalDesc");
  const modalStoreLink = document.getElementById("modalStoreLink");

  // Initialize
  function init() {
    populateFilters();
    renderCategoryChips();
    applyFiltersAndRender();
    setupEventListeners();
  }

  // Populate Dropdowns
  function populateFilters() {
    const categories = [...new Set(PRODUCTS_DATA.map(p => p.category))].sort();
    const brands = [...new Set(PRODUCTS_DATA.map(p => p.brand))].sort();

    categories.forEach(cat => {
      const opt = document.createElement("option");
      opt.value = cat;
      opt.textContent = cat;
      categoryFilter.appendChild(opt);
    });

    brands.forEach(br => {
      const opt = document.createElement("option");
      opt.value = br;
      opt.textContent = br;
      brandFilter.appendChild(opt);
    });
  }

  // Render Horizontal Category Chips
  function renderCategoryChips() {
    const categories = [...new Set(PRODUCTS_DATA.map(p => p.category))].sort();
    categories.forEach(cat => {
      const btn = document.createElement("button");
      btn.className = "chip";
      btn.dataset.cat = cat;
      btn.textContent = cat;
      categoryChips.appendChild(btn);
    });
  }

  // Filter & Sort Logic
  function getFilteredData() {
    return PRODUCTS_DATA.filter(p => {
      // Search
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        const match = 
          p.title.toLowerCase().includes(q) ||
          p.brand.toLowerCase().includes(q) ||
          p.category.toLowerCase().includes(q) ||
          p.pack_size.toLowerCase().includes(q) ||
          p.id.toLowerCase().includes(q);
        if (!match) return false;
      }

      // Category
      if (activeCategory && p.category !== activeCategory) {
        return false;
      }

      // Brand
      if (activeBrand && p.brand !== activeBrand) {
        return false;
      }

      // Margin
      if (activeMargin === "high" && p.margin_pct < 20) return false;
      if (activeMargin === "mid" && (p.margin_pct < 10 || p.margin_pct >= 20)) return false;
      if (activeMargin === "low" && p.margin_pct >= 10) return false;

      return true;
    });
  }

  function getSortedData(data) {
    if (!currentSort.field) return data;
    const { field, asc } = currentSort;
    return [...data].sort((a, b) => {
      let vA = a[field];
      let vB = b[field];

      if (typeof vA === "string") {
        vA = vA.toLowerCase();
        vB = vB.toLowerCase();
      }

      if (vA < vB) return asc ? -1 : 1;
      if (vA > vB) return asc ? 1 : -1;
      return 0;
    });
  }

  // Render Table & Update Statistics
  function applyFiltersAndRender() {
    const filtered = getFilteredData();
    const sorted = getSortedData(filtered);

    // Update Stats
    updateStats(filtered);

    // Render Table
    tableBody.innerHTML = "";

    if (sorted.length === 0) {
      emptyState.style.display = "block";
    } else {
      emptyState.style.display = "none";
      const fragment = document.createDocumentFragment();

      sorted.forEach((p, idx) => {
        const tr = document.createElement("tr");

        let marginClass = "margin-mid";
        if (p.margin_pct >= 20) marginClass = "margin-high";
        else if (p.margin_pct < 10) marginClass = "margin-low";

        tr.innerHTML = `
          <td style="text-align: center; color: var(--text-dim); font-size: 12px;">${idx + 1}</td>
          <td style="text-align: center;">
            <div class="thumb-wrapper" onclick="openModal('${p.id}')" title="Click to enlarge">
              <img class="thumb-img" src="${p.local_image}" alt="${p.title}" loading="lazy" onerror="this.onerror=null; this.src='${p.image_link}';" />
            </div>
          </td>
          <td>
            <div class="prod-cell">
              <div>
                <div class="prod-title" onclick="openModal('${p.id}')">${p.title}</div>
                <span class="pack-badge">${p.pack_size}</span>
              </div>
            </div>
          </td>
          <td><span class="category-badge">${p.category}</span></td>
          <td class="brand-cell">${p.brand}</td>
          <td style="color: var(--text-muted);">${p.pack_size}</td>
          <td class="num-cell buying-cost" style="text-align: right;">₹${p.buying_cost.toFixed(2)}</td>
          <td class="num-cell selling-price" style="text-align: right;">₹${p.price.toFixed(2)}</td>
          <td class="num-cell profit-cell" style="text-align: right;">+₹${p.profit.toFixed(2)}</td>
          <td style="text-align: center;">
            <span class="margin-pill ${marginClass}">${p.margin_pct}%</span>
          </td>
          <td style="text-align: center;">
            <span class="stock-badge"><span class="stock-dot"></span> In Stock</span>
          </td>
          <td style="text-align: center;">
            <a href="${p.link}" target="_blank" class="link-icon-btn" title="View in 1DD Store">
              <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path></svg>
            </a>
          </td>
        `;
        fragment.appendChild(tr);
      });
      tableBody.appendChild(fragment);
    }

    // Update Counts & Filter bar
    const isFiltered = searchQuery || activeCategory || activeBrand || activeMargin;
    resultsCount.textContent = `Showing ${sorted.length} of ${PRODUCTS_DATA.length} products`;
    resetFiltersBtn.style.display = isFiltered ? "inline-block" : "none";
  }

  function updateStats(data) {
    statTotalCount.textContent = data.length;
    statFilteredMeta.textContent = data.length === PRODUCTS_DATA.length ? "All catalog items" : `Filtered from ${PRODUCTS_DATA.length}`;

    const totalSelling = data.reduce((sum, p) => sum + p.price, 0);
    const totalCost = data.reduce((sum, p) => sum + p.buying_cost, 0);
    const totalProfit = totalSelling - totalCost;
    const avgMargin = totalSelling > 0 ? ((totalProfit / totalSelling) * 100).toFixed(1) : 0;

    statTotalSelling.textContent = "₹" + totalSelling.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    statTotalCost.textContent = "₹" + totalCost.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    statTotalProfit.textContent = "₹" + totalProfit.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    statAvgMargin.textContent = `Avg Gross Margin: ${avgMargin}%`;
  }

  // Modal Functionality
  function openModal(id) {
    const p = PRODUCTS_DATA.find(item => item.id === id);
    if (!p) return;

    modalTitle.textContent = p.title;
    modalImg.src = p.local_image;
    modalImg.onerror = () => { modalImg.src = p.image_link; };
    modalId.textContent = p.id;
    modalCategory.textContent = p.category;
    modalBrand.textContent = p.brand;
    modalPack.textContent = p.pack_size;
    modalCost.textContent = "₹" + p.buying_cost.toFixed(2);
    modalPrice.textContent = "₹" + p.price.toFixed(2);
    modalProfit.textContent = "+₹" + p.profit.toFixed(2) + ` (${p.markup_pct}% markup)`;
    modalMargin.textContent = p.margin_pct + "%";
    modalDesc.textContent = p.description;
    modalStoreLink.href = p.link;

    modal.classList.add("open");
  }

  function closeModal() {
    modal.classList.remove("open");
  }

  // Event Listeners Setup
  function setupEventListeners() {
    // Search input
    searchInput.addEventListener("input", (e) => {
      searchQuery = e.target.value.trim();
      applyFiltersAndRender();
    });

    // Category Select
    categoryFilter.addEventListener("change", (e) => {
      activeCategory = e.target.value;
      updateChipActive(activeCategory);
      applyFiltersAndRender();
    });

    // Category Chips
    categoryChips.addEventListener("click", (e) => {
      if (e.target.classList.contains("chip")) {
        activeCategory = e.target.dataset.cat || "";
        categoryFilter.value = activeCategory;
        updateChipActive(activeCategory);
        applyFiltersAndRender();
      }
    });

    function updateChipActive(cat) {
      document.querySelectorAll(".chip").forEach(c => {
        if ((c.dataset.cat || "") === cat) {
          c.classList.add("active");
          c.scrollIntoView({ behavior: "smooth", inline: "center", block: "nearest" });
        } else {
          c.classList.remove("active");
        }
      });
    }

    // Brand Select
    brandFilter.addEventListener("change", (e) => {
      activeBrand = e.target.value;
      applyFiltersAndRender();
    });

    // Margin Select
    marginFilter.addEventListener("change", (e) => {
      activeMargin = e.target.value;
      applyFiltersAndRender();
    });

    // Reset All Filters
    resetFiltersBtn.addEventListener("click", () => {
      searchQuery = "";
      activeCategory = "";
      activeBrand = "";
      activeMargin = "";
      searchInput.value = "";
      categoryFilter.value = "";
      brandFilter.value = "";
      marginFilter.value = "";
      updateChipActive("");
      applyFiltersAndRender();
    });

    // Sorting on Column Click
    document.querySelectorAll("th.sortable").forEach(th => {
      th.addEventListener("click", () => {
        const field = th.dataset.sort;
        if (currentSort.field === field) {
          currentSort.asc = !currentSort.asc;
        } else {
          currentSort.field = field;
          currentSort.asc = true;
        }

        // Update headers visual state
        document.querySelectorAll("th.sortable").forEach(h => {
          h.classList.remove("active-sort");
          h.querySelector(".sort-icon").innerHTML = "&uarr;&darr;";
        });
        th.classList.add("active-sort");
        th.querySelector(".sort-icon").innerHTML = currentSort.asc ? "&uarr;" : "&darr;";

        applyFiltersAndRender();
      });
    });

    // Density Buttons
    document.querySelectorAll(".segment-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        document.querySelectorAll(".segment-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        const density = btn.dataset.density;
        document.body.className = document.body.className.replace(/density-\\w+/g, "");
        if (density !== "normal") {
          document.body.classList.add(`density-${density}`);
        }
      });
    });

    // Theme Toggle
    themeToggle.addEventListener("click", () => {
      const isLight = document.documentElement.getAttribute("data-theme") === "light";
      if (isLight) {
        document.documentElement.removeAttribute("data-theme");
        localStorage.setItem("theme", "dark");
      } else {
        document.documentElement.setAttribute("data-theme", "light");
        localStorage.setItem("theme", "light");
      }
    });

    // Restore Theme Preference
    if (localStorage.getItem("theme") === "light") {
      document.documentElement.setAttribute("data-theme", "light");
    }

    // Modal Close
    modalCloseBtn.addEventListener("click", closeModal);
    modal.addEventListener("click", (e) => {
      if (e.target === modal) closeModal();
    });
    window.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("open")) closeModal();
    });

    // CSV Export
    exportCsvBtn.addEventListener("click", () => {
      const data = getFilteredData();
      if (data.length === 0) return;

      const headers = ["ID", "Title", "Brand", "Category", "Pack Size", "Buying Cost (INR)", "Selling Price (INR)", "Profit (INR)", "Margin %", "Store Link"];
      const csvRows = [headers.join(",")];

      data.forEach(p => {
        const row = [
          `"${p.id}"`,
          `"${p.title.replace(/"/g, '""')}"`,
          `"${p.brand.replace(/"/g, '""')}"`,
          `"${p.category.replace(/"/g, '""')}"`,
          `"${p.pack_size.replace(/"/g, '""')}"`,
          p.buying_cost.toFixed(2),
          p.price.toFixed(2),
          p.profit.toFixed(2),
          p.margin_pct,
          `"${p.link}"`
        ];
        csvRows.push(row.join(","));
      });

      const blob = new Blob([csvRows.join("\\n")], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `1DD_Product_Catalog_${new Date().toISOString().slice(0,10)}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    });
  }

  // Start app
  document.addEventListener("DOMContentLoaded", init);
</script>

</body>
</html>
"""

final_html = template.replace("__PRODUCTS_PLACEHOLDER__", products_json)

with open('products.html', 'w', encoding='utf-8') as f:
    f.write(final_html)

print("Successfully generated index.html with", len(products), "products.")
