import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/order.dart';
import '../models/product.dart';
import '../services/auth_service.dart';
import '../services/firestore_service.dart';
import '../services/meta_catalog_service.dart';
import '../config/location_constants.dart';

class SellerDashboardScreen extends StatefulWidget {
  const SellerDashboardScreen({super.key});

  @override
  State<SellerDashboardScreen> createState() => _SellerDashboardScreenState();
}

class _SellerDashboardScreenState extends State<SellerDashboardScreen> with SingleTickerProviderStateMixin {
  final FirestoreService _firestore = FirestoreService();
  late TabController _tabController;

  // Login controllers
  final _emailController = TextEditingController(text: "angsudas62@gmail.com");
  final _passwordController = TextEditingController();
  bool _isAuthLoading = false;

  // Meta API controllers
  final _metaCatalogIdController = TextEditingController();
  final _metaTokenController = TextEditingController();
  bool _isMetaSyncing = false;
  String _metaSyncStatusMessage = "";

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _metaCatalogIdController.dispose();
    _metaTokenController.dispose();
    super.dispose();
  }

  void _showAuthConfigHelp(String error) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Row(
          children: [
            Icon(Icons.settings_suggest, color: Color(0xFFF59E0B)),
            SizedBox(width: 8),
            Text("Auth Setup Required", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                "Firebase Authentication has not been activated or the Sign-in Provider is disabled for this project.",
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
              ),
              const SizedBox(height: 12),
              const Text(
                "To fix this once in Firebase Console:",
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Color(0xFF15803D)),
              ),
              const SizedBox(height: 6),
              const Text("1. Open Firebase Console for 'onedaydelivery-market'."),
              const Text("2. Go to Build > Authentication > Sign-in method."),
              const Text("3. Enable 'Google' and 'Email/Password' providers."),
              const Text("4. Ensure '1dd.web.app' is in Authorized Domains."),
              const SizedBox(height: 14),
              InkWell(
                onTap: () => launchUrl(
                  Uri.parse("https://console.firebase.google.com/project/onedaydelivery-market/authentication/providers"),
                  mode: LaunchMode.externalApplication,
                ),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                  decoration: BoxDecoration(
                    color: const Color(0xFFEFF6FF),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: const Color(0xFFBFDBFE)),
                  ),
                  child: const Row(
                    children: [
                      Icon(Icons.open_in_new, size: 16, color: Color(0xFF1D4ED8)),
                      SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          "Open Firebase Auth Console ↗",
                          style: TextStyle(color: Color(0xFF1D4ED8), fontWeight: FontWeight.bold, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 10),
              Text(
                "Technical error: $error",
                style: const TextStyle(fontSize: 11, color: Colors.grey),
              ),
            ],
          ),
        ),
        actions: [
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF15803D),
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.pop(ctx),
            child: const Text("Got It"),
          ),
        ],
      ),
    );
  }

  Future<void> _handleGoogleLogin(AuthService auth) async {
    setState(() => _isAuthLoading = true);
    try {
      final success = await auth.signInWithGoogle();
      if (!success) {
        return;
      }
      if (!auth.isAuthorizedOwner) {
        final email = auth.currentUser?.email ?? "Unknown";
        await auth.signOut();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              backgroundColor: Colors.red.shade700,
              content: Text("Access Denied: $email is not authorized as store owner."),
            ),
          );
        }
      }
    } catch (e) {
      final errStr = e.toString();
      if (mounted) {
        if (errStr.contains("configuration-not-found") || errStr.contains("CONFIGURATION_NOT_FOUND")) {
          _showAuthConfigHelp(errStr);
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              backgroundColor: Colors.red.shade700,
              content: Text("Google Sign-In failed: $e"),
            ),
          );
        }
      }
    } finally {
      if (mounted) {
        setState(() => _isAuthLoading = false);
      }
    }
  }

  Future<void> _handleLogin(AuthService auth) async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    if (email.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter both email and password")),
      );
      return;
    }

    if (!AuthService.authorizedOwners.contains(email.toLowerCase())) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Access Denied: $email is not an authorized owner email")),
      );
      return;
    }

    setState(() => _isAuthLoading = true);
    try {
      await auth.signInWithEmailPassword(email, password);
    } catch (e) {
      final errStr = e.toString();
      if (mounted) {
        if (errStr.contains("configuration-not-found") || errStr.contains("CONFIGURATION_NOT_FOUND")) {
          _showAuthConfigHelp(errStr);
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              backgroundColor: Colors.red.shade700,
              content: Text("Login error: $e. If this is your first time, click 'Register'."),
            ),
          );
        }
      }
    } finally {
      if (mounted) {
        setState(() => _isAuthLoading = false);
      }
    }
  }

  Future<void> _handleRegister(AuthService auth) async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    if (email.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter both email and a new password (min 6 characters)")),
      );
      return;
    }

    if (password.length < 6) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Password must be at least 6 characters long")),
      );
      return;
    }

    if (!AuthService.authorizedOwners.contains(email.toLowerCase())) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Access Denied: $email is not an authorized owner email")),
      );
      return;
    }

    setState(() => _isAuthLoading = true);
    try {
      await auth.registerWithEmailPassword(email, password);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: Color(0xFF15803D),
            content: Text("Owner account registered and signed in successfully!"),
          ),
        );
      }
    } catch (e) {
      final errStr = e.toString();
      if (mounted) {
        if (errStr.contains("configuration-not-found") || errStr.contains("CONFIGURATION_NOT_FOUND")) {
          _showAuthConfigHelp(errStr);
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              backgroundColor: Colors.red.shade700,
              content: Text("Registration error: $e"),
            ),
          );
        }
      }
    } finally {
      if (mounted) {
        setState(() => _isAuthLoading = false);
      }
    }
  }

  Future<void> _handleForgotPassword(AuthService auth) async {
    final email = _emailController.text.trim();
    if (email.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter your email above to receive a reset link")),
      );
      return;
    }
    try {
      await auth.sendPasswordReset(email);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: const Color(0xFF15803D),
            content: Text("Password reset email sent to $email. Please check your inbox."),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: Colors.red.shade700,
            content: Text("Error sending reset email: $e"),
          ),
        );
      }
    }
  }

  void _openAddProductDialog() {
    final titleCtrl = TextEditingController();
    final priceCtrl = TextEditingController();
    final unitCtrl = TextEditingController();
    final stockCtrl = TextEditingController(text: "20");
    final imgCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    ProductCategory selectedCategory = ProductCategory.VEGETABLES;

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          title: const Text("Add New Product", style: TextStyle(fontWeight: FontWeight.bold)),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: titleCtrl,
                  decoration: const InputDecoration(labelText: "Product Title *", hintText: "e.g. Red Spinach / Joha Rice"),
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<ProductCategory>(
                  value: selectedCategory,
                  decoration: const InputDecoration(labelText: "Category *"),
                  items: ProductCategory.values
                      .where((c) => c != ProductCategory.ALL)
                      .map((c) => DropdownMenuItem(value: c, child: Text("${c.iconEmoji} ${c.displayName}")))
                      .toList(),
                  onChanged: (c) {
                    if (c != null) setDialogState(() => selectedCategory = c);
                  },
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: priceCtrl,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: "Price (₹) *", hintText: "40"),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextField(
                        controller: unitCtrl,
                        decoration: const InputDecoration(labelText: "Unit *", hintText: "1 kg / 500g"),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: stockCtrl,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(labelText: "Initial Stock Quantity *", hintText: "20"),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: imgCtrl,
                  decoration: const InputDecoration(labelText: "Image URL", hintText: "https://..."),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: descCtrl,
                  maxLines: 2,
                  decoration: const InputDecoration(labelText: "Description", hintText: "Freshly harvested from Rangachakua..."),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text("Cancel"),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF15803D),
                foregroundColor: Colors.white,
              ),
              onPressed: () async {
                final title = titleCtrl.text.trim();
                final price = double.tryParse(priceCtrl.text.trim()) ?? 0.0;
                final unit = unitCtrl.text.trim();
                final stock = int.tryParse(stockCtrl.text.trim()) ?? 0;

                if (title.isEmpty || price <= 0 || unit.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("Please fill all required product fields")),
                  );
                  return;
                }

                final prodId = "prod_${DateTime.now().millisecondsSinceEpoch}";
                final newProd = Product(
                  id: prodId,
                  title: title,
                  description: descCtrl.text.trim(),
                  price: price,
                  unit: unit,
                  category: selectedCategory,
                  stockQuantity: stock,
                  imageUrl: imgCtrl.text.trim().isNotEmpty
                      ? imgCtrl.text.trim()
                      : "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600",
                );

                await _firestore.createProduct(newProd);

                // Auto-push to Meta Graph API if credentials are provided
                if (_metaCatalogIdController.text.trim().isNotEmpty && _metaTokenController.text.trim().isNotEmpty) {
                  MetaCatalogService.pushProductToMetaGraphApi(
                    product: newProd,
                    catalogId: _metaCatalogIdController.text.trim(),
                    accessToken: _metaTokenController.text.trim(),
                  );
                }

                if (ctx.mounted) Navigator.pop(ctx);
              },
              child: const Text("Save & Sync"),
            ),
          ],
        ),
      ),
    );
  }

  void _sendWhatsAppStatusUpdate(StoreOrder order, OrderStatus newStatus) async {
    await _firestore.updateOrderStatus(order.id, newStatus);
    final msg = MetaCatalogService.formatSellerStatusUpdateMessage(order, newStatus);
    await MetaCatalogService.launchWhatsApp(phone: order.customerPhone, message: msg);
  }

  @override
  Widget build(BuildContext context) {
    final auth = Provider.of<AuthService>(context);

    // If not authenticated or not authorized owner, show Owner Login Screen
    if (!auth.isAuthenticated || !auth.isAuthorizedOwner) {
      return Scaffold(
        appBar: AppBar(
          title: const Text("Owner Sign In"),
          backgroundColor: Colors.white,
          foregroundColor: Colors.black87,
          elevation: 0.5,
        ),
        body: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Container(
              constraints: const BoxConstraints(maxWidth: 420),
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0xFFE2E8F0)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.05),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Center(
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(16),
                      child: Image.asset(
                        'assets/images/logo.png',
                        width: 72,
                        height: 72,
                        fit: BoxFit.cover,
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    "Store House Owner Access",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontWeight: FontWeight.w900, fontSize: 20),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    "Restricted to verified Store House Owner (angsudas62@gmail.com).",
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.grey, fontSize: 12),
                  ),
                  const SizedBox(height: 20),
                  // Google Sign In Button
                  OutlinedButton(
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      side: const BorderSide(color: Color(0xFFCBD5E1), width: 1.5),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      backgroundColor: Colors.white,
                    ),
                    onPressed: _isAuthLoading ? null : () => _handleGoogleLogin(auth),
                    child: _isAuthLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Container(
                                width: 22,
                                height: 22,
                                decoration: const BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: Color(0xFF4285F4),
                                ),
                                child: const Center(
                                  child: Text(
                                    "G",
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontWeight: FontWeight.bold,
                                      fontSize: 14,
                                    ),
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                              const Text(
                                "Sign in with Google",
                                style: TextStyle(
                                  color: Color(0xFF1E293B),
                                  fontSize: 15,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                            ],
                          ),
                  ),

                  const SizedBox(height: 20),
                  Row(
                    children: [
                      const Expanded(child: Divider()),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 10),
                        child: Text(
                          "OR WITH EMAIL",
                          style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.grey.shade500),
                        ),
                      ),
                      const Expanded(child: Divider()),
                    ],
                  ),
                  const SizedBox(height: 16),

                  TextField(
                    controller: _emailController,
                    decoration: InputDecoration(
                      labelText: "Owner Email",
                      prefixIcon: const Icon(Icons.email_outlined),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _passwordController,
                    obscureText: true,
                    decoration: InputDecoration(
                      labelText: "Password",
                      prefixIcon: const Icon(Icons.lock_outline),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF15803D),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 13),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                          onPressed: _isAuthLoading ? null : () => _handleLogin(auth),
                          child: const Text("Sign In", style: TextStyle(fontWeight: FontWeight.bold)),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: OutlinedButton(
                          style: OutlinedButton.styleFrom(
                            foregroundColor: const Color(0xFF15803D),
                            side: const BorderSide(color: Color(0xFF15803D)),
                            padding: const EdgeInsets.symmetric(vertical: 13),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                          onPressed: _isAuthLoading ? null : () => _handleRegister(auth),
                          child: const Text("Register", style: TextStyle(fontWeight: FontWeight.bold)),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: _isAuthLoading ? null : () => _handleForgotPassword(auth),
                    child: const Text(
                      "Forgot password? Send reset link",
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
    }

    // Authenticated Owner View
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("Store House Owner Dashboard", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.black87)),
            Text("WhatsApp Commerce Hub • angsudas62@gmail.com", style: TextStyle(fontSize: 11, color: Color(0xFF15803D), fontWeight: FontWeight.bold)),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.grey),
            tooltip: "Sign Out",
            onPressed: () => auth.signOut(),
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          labelColor: const Color(0xFF15803D),
          unselectedLabelColor: Colors.grey,
          indicatorColor: const Color(0xFF15803D),
          indicatorWeight: 3,
          tabs: const [
            Tab(icon: Icon(Icons.inventory_2_outlined), text: "Inventory Catalog"),
            Tab(icon: Icon(Icons.receipt_long_outlined), text: "Customer Orders"),
            Tab(icon: Icon(Icons.sync_alt), text: "WhatsApp & Meta Sync"),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildInventoryTab(),
          _buildOrdersTab(),
          _buildMetaCatalogTab(),
        ],
      ),
      floatingActionButton: _tabController.index == 0
          ? FloatingActionButton.extended(
              backgroundColor: const Color(0xFF15803D),
              foregroundColor: Colors.white,
              icon: const Icon(Icons.add),
              label: const Text("Add Product"),
              onPressed: _openAddProductDialog,
            )
          : null,
    );
  }

  Widget _buildInventoryTab() {
    return StreamBuilder<List<Product>>(
      stream: _firestore.getProductsStream(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator(color: Color(0xFF15803D)));
        }

        final products = snapshot.data ?? [];
        final lowStock = products.where((p) => p.stockQuantity <= 5).length;

        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Metrics Cards
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard("Total Products", "${products.length}", Icons.inventory, Colors.black87),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard("Low Stock Alerts", "$lowStock", Icons.warning_amber_rounded, const Color(0xFFF59E0B)),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  "Catalog Items",
                  style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
                ),
                Text(
                  "${products.length} live items",
                  style: const TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
            const SizedBox(height: 10),
            ...products.map((prod) {
              return Card(
                elevation: 0.5,
                margin: const EdgeInsets.only(bottom: 10),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: Color(0xFFE2E8F0)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Row(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.network(
                          prod.imageUrl,
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            width: 60,
                            height: 60,
                            color: Colors.grey.shade100,
                            child: const Icon(Icons.image, color: Colors.grey),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(prod.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                            Text("₹${prod.price.toStringAsFixed(0)} / ${prod.unit}", style: const TextStyle(color: Color(0xFF15803D), fontWeight: FontWeight.bold)),
                            const SizedBox(height: 4),
                            Text("Stock: ${prod.stockQuantity} units", style: TextStyle(fontSize: 12, color: prod.stockQuantity <= 5 ? Colors.orange : Colors.grey)),
                          ],
                        ),
                      ),
                      Row(
                        children: [
                          IconButton(
                            icon: const Icon(Icons.chat, color: Color(0xFF25D366), size: 20),
                            tooltip: "Share to WhatsApp",
                            onPressed: () {
                              MetaCatalogService.launchWhatsApp(
                                phone: LocationConstants.storePhone,
                                message: MetaCatalogService.formatProductWhatsAppShareText(prod),
                              );
                            },
                          ),
                          IconButton(
                            icon: const Icon(Icons.edit_note, color: Colors.blue),
                            tooltip: "Quick Stock Edit",
                            onPressed: () {
                              final ctrl = TextEditingController(text: "${prod.stockQuantity}");
                              showDialog(
                                context: context,
                                builder: (ctx) => AlertDialog(
                                  title: Text("Update Stock: ${prod.title}"),
                                  content: TextField(
                                    controller: ctrl,
                                    keyboardType: TextInputType.number,
                                    decoration: const InputDecoration(labelText: "New Stock Quantity"),
                                  ),
                                  actions: [
                                    TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Cancel")),
                                    ElevatedButton(
                                      onPressed: () async {
                                        final newQty = int.tryParse(ctrl.text.trim()) ?? prod.stockQuantity;
                                        await _firestore.updateProductStock(prod.id, newQty);
                                        if (ctx.mounted) Navigator.pop(ctx);
                                      },
                                      child: const Text("Save"),
                                    )
                                  ],
                                ),
                              );
                            },
                          ),
                          IconButton(
                            icon: const Icon(Icons.delete_outline, color: Colors.red),
                            tooltip: "Delete Product",
                            onPressed: () async {
                              final confirm = await showDialog<bool>(
                                context: context,
                                builder: (ctx) => AlertDialog(
                                  title: const Text("Delete Product?"),
                                  content: Text("Are you sure you want to delete '${prod.title}'?"),
                                  actions: [
                                    TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text("Cancel")),
                                    ElevatedButton(
                                      style: ElevatedButton.styleFrom(backgroundColor: Colors.red, foregroundColor: Colors.white),
                                      onPressed: () => Navigator.pop(ctx, true),
                                      child: const Text("Delete"),
                                    ),
                                  ],
                                ),
                              );
                              if (confirm == true) {
                                await _firestore.deleteProduct(prod.id);
                              }
                            },
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            }),
            const SizedBox(height: 80),
          ],
        );
      },
    );
  }

  Widget _buildOrdersTab() {
    return StreamBuilder<List<StoreOrder>>(
      stream: _firestore.getOrdersStream(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator(color: Color(0xFF15803D)));
        }

        final orders = snapshot.data ?? [];
        final totalRev = orders.where((o) => o.status != OrderStatus.CANCELLED).fold(0.0, (s, o) => s + o.totalAmount);
        final activeCount = orders.where((o) => o.status != OrderStatus.CANCELLED && o.status != OrderStatus.DELIVERED).length;

        if (orders.isEmpty) {
          return const Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.inbox_outlined, size: 48, color: Colors.grey),
                SizedBox(height: 12),
                Text("No orders placed yet", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                Text("Incoming orders from WhatsApp or web will appear here in real time.", style: TextStyle(color: Colors.grey, fontSize: 12)),
              ],
            ),
          );
        }

        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Metrics Cards
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard("Active Deliveries", "$activeCount", Icons.delivery_dining, const Color(0xFF2563EB)),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard("Total Revenue", "₹${totalRev.toStringAsFixed(0)}", Icons.payments_outlined, const Color(0xFF15803D)),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              "Customer Order Feed & WhatsApp Status Dispatch",
              style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
            ),
            const SizedBox(height: 10),
            ...orders.map((order) {
              return Card(
                elevation: 0.5,
                margin: const EdgeInsets.only(bottom: 14),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14),
                  side: const BorderSide(color: Color(0xFFE2E8F0)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(order.orderNumber, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
                          _buildStatusBadge(order.status),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text("👤 Customer: ${order.customerName} (${order.customerPhone})", style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                      Text("📍 Destination: ${order.deliveryAddress}", style: const TextStyle(fontSize: 12, color: Colors.black87)),
                      Text("⏱️ Haversine: ${order.distanceKm} km (~${order.estimatedMinutes} mins) • 🔐 OTP: ${order.otpCode}", style: const TextStyle(fontSize: 12, color: Colors.grey)),
                      const Divider(height: 16),
                      ...order.items.map((it) => Text("• ${it.productTitle} × ${it.quantity} (${it.unit}) = ₹${it.subtotal.toStringAsFixed(0)}", style: const TextStyle(fontSize: 12))),
                      const SizedBox(height: 6),
                      Text(
                        "Total: ₹${order.totalAmount.toStringAsFixed(0)} (${order.paymentMethod})",
                        style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 14, color: Color(0xFF15803D)),
                      ),
                      const SizedBox(height: 12),

                      // Status & WhatsApp Actions
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          if (order.status == OrderStatus.PLACED)
                            ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFFEF3C7), foregroundColor: const Color(0xFF92400E)),
                              icon: const Icon(Icons.check_circle_outline, size: 16),
                              label: const Text("Accept & Prepare"),
                              onPressed: () => _sendWhatsAppStatusUpdate(order, OrderStatus.PREPARING),
                            ),
                          if (order.status == OrderStatus.PREPARING)
                            ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFE0E7FF), foregroundColor: const Color(0xFF3730A3)),
                              icon: const Icon(Icons.two_wheeler, size: 16),
                              label: const Text("Out for Delivery"),
                              onPressed: () => _sendWhatsAppStatusUpdate(order, OrderStatus.OUT_FOR_DELIVERY),
                            ),
                          if (order.status == OrderStatus.OUT_FOR_DELIVERY)
                            ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFDCFCE7), foregroundColor: const Color(0xFF166534)),
                              icon: const Icon(Icons.done_all, size: 16),
                              label: const Text("Mark Delivered"),
                              onPressed: () => _sendWhatsAppStatusUpdate(order, OrderStatus.DELIVERED),
                            ),
                          // 1-Tap WhatsApp Message Customer
                          if (order.customerPhone.isNotEmpty)
                            ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF25D366), foregroundColor: Colors.white),
                              icon: const Icon(Icons.chat, size: 16),
                              label: const Text("WhatsApp Buyer"),
                              onPressed: () {
                                final msg = MetaCatalogService.formatSellerStatusUpdateMessage(order, order.status);
                                MetaCatalogService.launchWhatsApp(phone: order.customerPhone, message: msg);
                              },
                            ),
                          if (order.customerPhone.isNotEmpty)
                            OutlinedButton.icon(
                              icon: const Icon(Icons.phone, size: 16),
                              label: const Text("Call"),
                              onPressed: () => launchUrl(Uri.parse("tel:${order.customerPhone}")),
                            ),
                          if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED)
                            TextButton(
                              onPressed: () => _firestore.updateOrderStatus(order.id, OrderStatus.CANCELLED),
                              child: const Text("Cancel Order", style: TextStyle(color: Colors.red, fontSize: 12)),
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            }),
            const SizedBox(height: 80),
          ],
        );
      },
    );
  }

  Widget _buildMetaCatalogTab() {
    return StreamBuilder<List<Product>>(
      stream: _firestore.getProductsStream(),
      builder: (context, snapshot) {
        final products = snapshot.data ?? [];

        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Card 1: 100% Free Scheduled Data Feed URL (Recommended Method)
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: const BorderSide(color: Color(0xFF86EFAC))),
              color: const Color(0xFFF0FDF4),
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.link, color: Color(0xFF15803D)),
                        SizedBox(width: 8),
                        Text(
                          "Method 1: Live Meta Scheduled Feed (100% Free)",
                          style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16, color: Color(0xFF166534)),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      "Paste this Live CSV Feed URL into Meta Commerce Manager > Data Sources > Scheduled Feed. Meta will automatically fetch and sync all your products with your WhatsApp Catalog on schedule.",
                      style: TextStyle(fontSize: 13, color: Colors.black87),
                    ),
                    const SizedBox(height: 12),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFFCBD5E1)),
                      ),
                      child: const Row(
                        children: [
                          Expanded(
                            child: Text(
                              "https://1dd.web.app/whatsapp-catalog.csv",
                              style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.bold, fontSize: 13, color: Color(0xFF0F172A)),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 12),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF15803D), foregroundColor: Colors.white),
                          icon: const Icon(Icons.copy, size: 16),
                          label: const Text("Copy Feed URL"),
                          onPressed: () {
                            Clipboard.setData(const ClipboardData(text: "https://1dd.web.app/whatsapp-catalog.csv"));
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text("Copied Meta Feed URL to clipboard!")),
                            );
                          },
                        ),
                        OutlinedButton.icon(
                          icon: const Icon(Icons.file_download_outlined, size: 16),
                          label: const Text("Copy Raw Meta CSV to Clipboard"),
                          onPressed: () {
                            final csvContent = MetaCatalogService.generateMetaCommerceCsv(products);
                            Clipboard.setData(ClipboardData(text: csvContent));
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text("Copied ${products.length} products in Meta CSV format!")),
                            );
                          },
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Card 2: Real-time Meta Graph API Push (When API details added)
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: const BorderSide(color: Color(0xFFE2E8F0))),
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.bolt, color: Color(0xFF2563EB)),
                        SizedBox(width: 8),
                        Text(
                          "Method 2: Meta Graph API (Real-Time Push)",
                          style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      "Whenever you are ready to use Meta Graph API, enter your Meta Catalog ID & System User Token below to push items instantly:",
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _metaCatalogIdController,
                      decoration: const InputDecoration(
                        labelText: "Meta Commerce Catalog ID",
                        hintText: "e.g. 123456789012345",
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.store),
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _metaTokenController,
                      obscureText: true,
                      decoration: const InputDecoration(
                        labelText: "Meta System User Access Token (EAA...)",
                        hintText: "EAABw...",
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.vpn_key),
                      ),
                    ),
                    const SizedBox(height: 12),
                    if (_metaSyncStatusMessage.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Text(
                          _metaSyncStatusMessage,
                          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF15803D)),
                        ),
                      ),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF2563EB), foregroundColor: Colors.white),
                      icon: _isMetaSyncing
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                          : const Icon(Icons.cloud_upload_outlined, size: 18),
                      label: Text(_isMetaSyncing ? "Syncing..." : "⚡ Push All ${products.length} Products to Meta Catalog"),
                      onPressed: _isMetaSyncing
                          ? null
                          : () async {
                              final catId = _metaCatalogIdController.text.trim();
                              final token = _metaTokenController.text.trim();

                              if (catId.isEmpty || token.isEmpty) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text("Please fill Meta Catalog ID and Access Token (or use Method 1 above for free)")),
                                );
                                return;
                              }

                              setState(() {
                                _isMetaSyncing = true;
                                _metaSyncStatusMessage = "Syncing ${products.length} items to Meta...";
                              });

                              int successCount = 0;
                              for (final prod in products) {
                                final res = await MetaCatalogService.pushProductToMetaGraphApi(
                                  product: prod,
                                  catalogId: catId,
                                  accessToken: token,
                                );
                                if (res['success'] == true) successCount++;
                              }

                              setState(() {
                                _isMetaSyncing = false;
                                _metaSyncStatusMessage = "Sync complete: $successCount / ${products.length} items pushed to Meta.";
                              });
                            },
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Card 3: WhatsApp Business Quick Reply Templates
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: const BorderSide(color: Color(0xFFE2E8F0))),
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.chat_bubble_outline, color: Color(0xFF15803D)),
                        SizedBox(width: 8),
                        Text(
                          "WhatsApp Business Quick Reply Templates",
                          style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      "Copy and add these templates into your WhatsApp Business App > Business Tools > Quick Replies for 1-tap customer chat replies:",
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                    const SizedBox(height: 10),
                    _buildTemplateRow("Welcome / Shop", "👋 Welcome to 1DD Rangachakua Store! Browse our catalog or reply with the items you need for 1-Day delivery."),
                    _buildTemplateRow("UPI Payment", "💳 You can pay via UPI to: 9864012345@upi or scan our store QR code upon delivery."),
                    _buildTemplateRow("Delivery Status", "🚚 Your order is on the way with our 1DD rider! Please keep your OTP ready."),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 40),
          ],
        );
      },
    );
  }

  Widget _buildTemplateRow(String title, String message) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(color: const Color(0xFFF8FAFC), borderRadius: BorderRadius.circular(8), border: Border.all(color: const Color(0xFFE2E8F0))),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Color(0xFF15803D))),
                const SizedBox(height: 2),
                Text(message, style: const TextStyle(fontSize: 12)),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.copy, size: 16, color: Colors.grey),
            tooltip: "Copy Template",
            onPressed: () {
              Clipboard.setData(ClipboardData(text: message));
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text("Copied '$title' template!")),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildMetricCard(String title, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(value, style: TextStyle(fontWeight: FontWeight.w900, fontSize: 20, color: color)),
              Text(title, style: const TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBadge(OrderStatus status) {
    Color bg = const Color(0xFFE2E8F0);
    Color fg = const Color(0xFF334155);

    switch (status) {
      case OrderStatus.PLACED:
        bg = const Color(0xFFFEF3C7);
        fg = const Color(0xFF92400E);
        break;
      case OrderStatus.PREPARING:
        bg = const Color(0xFFE0E7FF);
        fg = const Color(0xFF3730A3);
        break;
      case OrderStatus.OUT_FOR_DELIVERY:
        bg = const Color(0xFFDCFCE7);
        fg = const Color(0xFF166534);
        break;
      case OrderStatus.DELIVERED:
        bg = const Color(0xFFD1FAE5);
        fg = const Color(0xFF065F46);
        break;
      case OrderStatus.CANCELLED:
        bg = const Color(0xFFFEE2E2);
        fg = const Color(0xFF991B1B);
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        status.displayName,
        style: TextStyle(color: fg, fontWeight: FontWeight.bold, fontSize: 11),
      ),
    );
  }
}
