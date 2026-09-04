import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../config/location_constants.dart';
import '../models/order.dart';
import '../providers/cart_provider.dart';
import '../services/firestore_service.dart';
import '../screens/order_tracker_dialog.dart';

class CartCheckoutSheet extends StatefulWidget {
  const CartCheckoutSheet({super.key});

  @override
  State<CartCheckoutSheet> createState() => _CartCheckoutSheetState();
}

class _CartCheckoutSheetState extends State<CartCheckoutSheet> {
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _notesController = TextEditingController();
  bool _isPlacingOrder = false;

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _addressController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _handlePlaceOrder(CartProvider cart) async {
    final name = _nameController.text.trim();
    final phone = _phoneController.text.trim();
    final address = _addressController.text.trim();
    final notes = _notesController.text.trim();

    if (cart.items.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Your cart is empty")),
      );
      return;
    }

    if (name.isEmpty || phone.isEmpty || address.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please fill name, phone, and delivery address")),
      );
      return;
    }

    setState(() => _isPlacingOrder = true);

    try {
      final orderId = "order_${DateTime.now().millisecondsSinceEpoch}";
      final orderNumber = "1DD-${(1000 + (DateTime.now().microsecond % 9000))}";
      final landmark = cart.selectedLandmark;
      final distance = cart.deliveryDistanceKm;
      final eta = cart.estimatedDeliveryMinutes;

      final storeOrder = StoreOrder(
        id: orderId,
        orderNumber: orderNumber,
        customerName: name,
        customerPhone: phone,
        deliveryAddress: "$address, near ${landmark.name}",
        deliveryLat: landmark.latitude,
        deliveryLng: landmark.longitude,
        landmarkName: landmark.name,
        items: cart.toOrderItems(),
        subtotal: cart.subtotal,
        deliveryFee: cart.deliveryFee,
        totalAmount: cart.totalAmount,
        status: OrderStatus.PLACED,
        orderTimestamp: DateTime.now().millisecondsSinceEpoch,
        deliveryPromise: "1-Day Express Local Delivery",
        deliveryNotes: notes,
        paymentMethod: "Cash on Delivery",
        paymentStatus: "Pending on Delivery",
        distanceKm: distance,
        estimatedMinutes: eta,
        deliveryPartnerName: "Store House Partner",
        deliveryPartnerPhone: LocationConstants.storePhone,
        otpCode: "${(1000 + (DateTime.now().millisecond % 9000))}",
      );

      final firestore = FirestoreService();
      await firestore.placeOrder(storeOrder);

      cart.clear();
      if (!mounted) return;

      Navigator.pop(context); // Close checkout sheet

      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (ctx) => OrderTrackerDialog(orderId: orderId),
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Failed to place order: $e")),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isPlacingOrder = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = Provider.of<CartProvider>(context);

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.88,
        maxWidth: 600,
      ),
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Icon(Icons.shopping_bag_outlined, color: Color(0xFF15803D)),
                  const SizedBox(width: 8),
                  Text(
                    "Your Order Cart (${cart.totalCount})",
                    style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18),
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              )
            ],
          ),
          const Divider(),
          Flexible(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Cart Items
                  if (cart.items.isEmpty)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: 32),
                      child: Center(
                        child: Text(
                          "Your cart is currently empty.",
                          style: TextStyle(color: Colors.grey),
                        ),
                      ),
                    )
                  else
                    ...cart.items.values.map((item) {
                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 6),
                        child: Row(
                          children: [
                            ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: Image.network(
                                item.product.imageUrl,
                                width: 50,
                                height: 50,
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) => Container(
                                  width: 50,
                                  height: 50,
                                  color: Colors.grey.shade200,
                                  child: const Icon(Icons.image, size: 24, color: Colors.grey),
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    item.product.title,
                                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                                  ),
                                  Text(
                                    "₹${item.product.price.toStringAsFixed(0)} × ${item.quantity} = ₹${item.subtotal.toStringAsFixed(0)}",
                                    style: TextStyle(color: Colors.grey.shade700, fontSize: 13),
                                  ),
                                ],
                              ),
                            ),
                            Row(
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.remove_circle_outline, size: 22),
                                  onPressed: () => cart.decrementQuantity(item.product.id),
                                ),
                                Text(
                                  "${item.quantity}",
                                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.add_circle_outline, size: 22),
                                  onPressed: () => cart.addToCart(item.product),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    }),

                  const Divider(height: 24),

                  // Destination Landmark Selector
                  const Text(
                    "1. Select Delivery Destination",
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<LocalLandmark>(
                    value: cart.selectedLandmark,
                    decoration: InputDecoration(
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                    items: presetLocalDestinations.map((lm) {
                      return DropdownMenuItem(
                        value: lm,
                        child: Text("${lm.name} (${lm.areaTag})", style: const TextStyle(fontSize: 13)),
                      );
                    }).toList(),
                    onChanged: (lm) {
                      if (lm != null) cart.setLandmark(lm);
                    },
                  ),
                  const SizedBox(height: 10),

                  // Haversine Distance Card
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF1F5F9),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: const Color(0xFF15803D).withOpacity(0.3)),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text("Haversine Delivery Distance:", style: TextStyle(fontSize: 12, color: Colors.grey)),
                            Text(
                              "${cart.deliveryDistanceKm.toStringAsFixed(2)} km",
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Color(0xFF15803D)),
                            ),
                          ],
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          decoration: BoxDecoration(
                            color: const Color(0xFFFEF3C7),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            "⏱️ ~${cart.estimatedDeliveryMinutes} mins guaranteed",
                            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF92400E)),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const Divider(height: 24),

                  // Customer Contact Info
                  const Text(
                    "2. Customer Delivery Details",
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _nameController,
                    decoration: InputDecoration(
                      labelText: "Your Name *",
                      hintText: "e.g. Rahul Sharma",
                      prefixIcon: const Icon(Icons.person_outline),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _phoneController,
                    keyboardType: TextInputType.phone,
                    decoration: InputDecoration(
                      labelText: "Phone Number *",
                      hintText: "+91 98765 43210",
                      prefixIcon: const Icon(Icons.phone_outlined),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _addressController,
                    decoration: InputDecoration(
                      labelText: "Specific Street Address / House # *",
                      hintText: "House #, Lane, Landmark",
                      prefixIcon: const Icon(Icons.home_outlined),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),
                  const SizedBox(height: 10),
                  TextField(
                    controller: _notesController,
                    decoration: InputDecoration(
                      labelText: "Delivery Notes (Optional)",
                      hintText: "e.g. Ring bell, leave near gate",
                      prefixIcon: const Icon(Icons.note_alt_outlined),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Bill Summary Card
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text("Items Subtotal:"),
                            Text("₹${cart.subtotal.toStringAsFixed(0)}", style: const TextStyle(fontWeight: FontWeight.bold)),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text("1-Day Express Delivery:"),
                            Text("₹${cart.deliveryFee.toStringAsFixed(0)}", style: const TextStyle(fontWeight: FontWeight.bold)),
                          ],
                        ),
                        const Divider(height: 16),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text("Total Payable (Cash on Delivery):", style: TextStyle(fontWeight: FontWeight.w900)),
                            Text(
                              "₹${cart.totalAmount.toStringAsFixed(0)}",
                              style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18, color: Color(0xFF15803D)),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Confirm Order Button
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF15803D),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 16),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: _isPlacingOrder || cart.items.isEmpty
                ? null
                : () => _handlePlaceOrder(cart),
            child: _isPlacingOrder
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                  )
                : const Text(
                    "Confirm 1-Day Order (Cash on Delivery)",
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                  ),
          ),
        ],
      ),
    );
  }
}
