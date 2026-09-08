import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../config/location_constants.dart';
import '../models/product.dart';
import '../providers/cart_provider.dart';
import '../services/firestore_service.dart';
import '../services/meta_catalog_service.dart';
import '../widgets/cart_checkout_sheet.dart';
import 'seller_dashboard_screen.dart';

class BuyerHomeScreen extends StatefulWidget {
  const BuyerHomeScreen({super.key});

  @override
  State<BuyerHomeScreen> createState() => _BuyerHomeScreenState();
}

class _BuyerHomeScreenState extends State<BuyerHomeScreen> {
  final FirestoreService _firestore = FirestoreService();
  String _selectedCategory = "ALL";
  String _searchQuery = "";
  final TextEditingController _searchController = TextEditingController();

  void _openCartSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => const CartCheckoutSheet(),
    );
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final cart = Provider.of<CartProvider>(context);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        title: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Image.asset(
                'assets/images/logo.png',
                width: 38,
                height: 38,
                fit: BoxFit.cover,
              ),
            ),
            const SizedBox(width: 10),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "1DD",
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.black87),
                ),
                Text(
                  "1-Day Express Delivery",
                  style: TextStyle(fontSize: 11, color: Color(0xFF15803D), fontWeight: FontWeight.bold),
                ),
              ],
            ),
          ],
        ),
        actions: [
          // Owner Dashboard Button
          TextButton.icon(
            style: TextButton.styleFrom(
              backgroundColor: const Color(0xFFF1F5F9),
              foregroundColor: const Color(0xFF0F172A),
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            ),
            icon: const Icon(Icons.storefront, size: 16, color: Color(0xFF15803D)),
            label: const Text(
              "Seller Login",
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
            ),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SellerDashboardScreen()),
              );
            },
          ),
          const SizedBox(width: 6),
          // Cart Button
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.shopping_cart_outlined, color: Colors.black87),
                onPressed: _openCartSheet,
              ),
              if (cart.totalCount > 0)
                Positioned(
                  right: 6,
                  top: 6,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: Color(0xFF15803D),
                      shape: BoxShape.circle,
                    ),
                    child: Text(
                      "${cart.totalCount}",
                      style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: StreamBuilder<List<Product>>(
        stream: _firestore.getProductsStream(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator(color: Color(0xFF15803D)));
          }

          final products = snapshot.data ?? [];

          // Extract dynamic categories based on first word of title
          final categorySet = <String>{};
          for (final p in products) {
            final firstWord = p.title.split("|").first.trim();
            if (firstWord.isNotEmpty) {
              categorySet.add(firstWord);
            }
          }
          final sortedCategories = ["ALL", ...categorySet.toList()..sort()];

          final filtered = products.where((p) {
            final firstWord = p.title.split("|").first.trim();
            final matchCat = _selectedCategory == "ALL" || firstWord.toLowerCase() == _selectedCategory.toLowerCase();
            final matchSearch = _searchQuery.isEmpty ||
                p.title.toLowerCase().contains(_searchQuery) ||
                p.description.toLowerCase().contains(_searchQuery);
            return matchCat && matchSearch;
          }).toList();

          return CustomScrollView(
            slivers: [
              // Depot Hero Banner
              SliverToBoxAdapter(
                child: Container(
                  margin: const EdgeInsets.all(16),
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF15803D), Color(0xFF065F46)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.08),
                        blurRadius: 10,
                        offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        "Rangachakua Store House",
                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 18),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        "Shop local vegetables, groceries, and staples. Order directly through WhatsApp or web with 1-Day guaranteed delivery.",
                        style: TextStyle(color: Colors.white70, fontSize: 12),
                      ),
                      const SizedBox(height: 10),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          "📍 Depot: ${LocationConstants.storeOriginLat}° N, ${LocationConstants.storeOriginLng}° E",
                          style: const TextStyle(color: Colors.white, fontFamily: 'monospace', fontSize: 11),
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              // Search Bar
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: TextField(
                    controller: _searchController,
                    onChanged: (val) {
                      setState(() => _searchQuery = val.toLowerCase().trim());
                    },
                    decoration: InputDecoration(
                      hintText: "Search items, groceries, vegetables...",
                      prefixIcon: const Icon(Icons.search, color: Colors.grey),
                      suffixIcon: _searchQuery.isNotEmpty
                          ? IconButton(
                              icon: const Icon(Icons.clear, size: 18),
                              onPressed: () {
                                _searchController.clear();
                                setState(() => _searchQuery = "");
                              },
                            )
                          : null,
                      filled: true,
                      fillColor: Colors.white,
                      contentPadding: const EdgeInsets.symmetric(vertical: 12),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(color: Color(0xFFE2E8F0)),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(color: Color(0xFFE2E8F0)),
                      ),
                    ),
                  ),
                ),
              ),

              // Category Chips Bar
              SliverToBoxAdapter(
                child: Container(
                  height: 48,
                  margin: const EdgeInsets.symmetric(vertical: 12),
                  child: ListView(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: sortedCategories.map((cat) {
                      final isSelected = _selectedCategory == cat;
                      final labelText = cat == "ALL" ? "🛒 All Items" : cat;
                      return Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: FilterChip(
                          selected: isSelected,
                          label: Text(labelText),
                          labelStyle: TextStyle(
                            fontSize: 12,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            color: isSelected ? Colors.white : Colors.black87,
                          ),
                          selectedColor: const Color(0xFF15803D),
                          backgroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                            side: BorderSide(
                              color: isSelected ? const Color(0xFF15803D) : const Color(0xFFE2E8F0),
                            ),
                          ),
                          onSelected: (_) {
                            setState(() => _selectedCategory = cat);
                          },
                        ),
                      );
                    }).toList(),
                  ),
                ),
              ),

              if (filtered.isEmpty)
                const SliverFillRemaining(
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.inventory_2_outlined, size: 48, color: Colors.grey),
                        SizedBox(height: 12),
                        Text(
                          "No products found",
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                        Text(
                          "Try searching for another item or resetting the category filter.",
                          style: TextStyle(color: Colors.grey, fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  sliver: SliverGrid(
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: MediaQuery.of(context).size.width > 900
                          ? 4
                          : (MediaQuery.of(context).size.width > 600 ? 3 : 2),
                      childAspectRatio: 0.68,
                      crossAxisSpacing: 12,
                      mainAxisSpacing: 12,
                    ),
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final product = filtered[index];
                        return _buildProductCard(context, product, cart);
                      },
                      childCount: filtered.length,
                    ),
                  ),
                ),
              const SliverToBoxAdapter(child: SizedBox(height: 80)),
            ],
          );
        },
      ),

      // Floating Action Button: Cart or Direct WhatsApp
      floatingActionButton: cart.totalCount > 0
          ? FloatingActionButton.extended(
              backgroundColor: const Color(0xFF15803D),
              foregroundColor: Colors.white,
              icon: const Icon(Icons.shopping_bag),
              label: Text("Cart (${cart.totalCount}) • ₹${cart.totalAmount.toStringAsFixed(0)}"),
              onPressed: _openCartSheet,
            )
          : FloatingActionButton.extended(
              backgroundColor: const Color(0xFF25D366),
              foregroundColor: Colors.white,
              icon: const Icon(Icons.chat),
              label: const Text("Chat & Order on WhatsApp"),
              onPressed: () {
                MetaCatalogService.launchWhatsApp(
                  phone: LocationConstants.storePhone,
                  message: "👋 Hi, I want to order fresh groceries from Rangachakua Store House.",
                );
              },
            ),
    );
  }

  Widget _buildProductCard(BuildContext context, Product product, CartProvider cart) {
    final isOutOfStock = product.stockQuantity <= 0;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Image + Badges + WhatsApp Share
          Expanded(
            flex: 5,
            child: Stack(
              children: [
                ClipRRect(
                  borderRadius: const BorderRadius.vertical(top: Radius.circular(14)),
                  child: Image.network(
                    product.imageUrl,
                    width: double.infinity,
                    height: double.infinity,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => Container(
                      color: Colors.grey.shade100,
                      child: const Center(
                        child: Icon(Icons.shopping_basket, size: 36, color: Colors.grey),
                      ),
                    ),
                  ),
                ),
                Positioned(
                  top: 6,
                  left: 6,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFF15803D),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text(
                          "⚡ 1-DAY",
                          style: TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w900),
                        ),
                      ),
                      if (product.isLocalSpecialty) ...[
                        const SizedBox(height: 2),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: const Color(0xFFF59E0B),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            "⭐ SPECIAL",
                            style: TextStyle(color: Color(0xFF78350F), fontSize: 9, fontWeight: FontWeight.w900),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                // Quick WhatsApp Share Button on card
                Positioned(
                  top: 6,
                  right: 6,
                  child: InkWell(
                    onTap: () {
                      MetaCatalogService.launchWhatsApp(
                        phone: LocationConstants.storePhone,
                        message: MetaCatalogService.formatProductWhatsAppShareText(product),
                      );
                    },
                    child: Container(
                      padding: const EdgeInsets.all(5),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.9),
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 4),
                        ],
                      ),
                      child: const Icon(Icons.chat, size: 16, color: Color(0xFF25D366)),
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Details
          Expanded(
            flex: 5,
            child: Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        product.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      Text(
                        product.description,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(color: Colors.grey.shade600, fontSize: 11),
                      ),
                    ],
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        "₹${product.price.toStringAsFixed(0)}",
                        style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 15, color: Color(0xFF15803D)),
                      ),
                      Text(
                        product.unit,
                        style: TextStyle(color: Colors.grey.shade600, fontSize: 10),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: SizedBox(
                          height: 32,
                          child: ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF15803D),
                              foregroundColor: Colors.white,
                              disabledBackgroundColor: Colors.grey.shade300,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                              padding: EdgeInsets.zero,
                            ),
                            onPressed: isOutOfStock
                                ? null
                                : () {
                                    cart.addToCart(product);
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      SnackBar(
                                        content: Text("Added ${product.title} to cart"),
                                        duration: const Duration(seconds: 1),
                                        action: SnackBarAction(
                                          label: "View Cart",
                                          textColor: Colors.white,
                                          onPressed: _openCartSheet,
                                        ),
                                      ),
                                    );
                                  },
                            child: Text(
                              isOutOfStock ? "Sold Out" : "+ Add to Cart",
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

extension FilterList<T> on List<T> {
  List<T> filter(bool Function(T) test) {
    final result = <T>[];
    for (final element in this) {
      if (test(element)) result.add(element);
    }
    return result;
  }
}
