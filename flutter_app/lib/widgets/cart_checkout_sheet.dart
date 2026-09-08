import 'dart:html' as html;
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../config/location_constants.dart';
import '../models/order.dart';
import '../providers/cart_provider.dart';
import '../services/firestore_service.dart';
import '../services/haversine_service.dart';
import '../screens/order_tracker_dialog.dart';

class CartCheckoutSheet extends StatefulWidget {
  const CartCheckoutSheet({super.key});

  @override
  State<CartCheckoutSheet> createState() => _CartCheckoutSheetState();
}

class _CartCheckoutSheetState extends State<CartCheckoutSheet> {
  final _slNumberController = TextEditingController();
  final _phoneController = TextEditingController();
  final _locationController = TextEditingController();
  final _nameController = TextEditingController();
  final _notesController = TextEditingController();

  bool _isPlacingOrder = false;
  bool _isLocating = false;
  double? _gpsLat;
  double? _gpsLng;

  @override
  void dispose() {
    _slNumberController.dispose();
    _phoneController.dispose();
    _locationController.dispose();
    _nameController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _detectCurrentLocation() async {
    if (!kIsWeb) return;
    setState(() => _isLocating = true);
    try {
      final position = await html.window.navigator.geolocation.getCurrentPosition();
      final lat = position.coords?.latitude?.toDouble();
      final lng = position.coords?.longitude?.toDouble();
      if (lat != null && lng != null) {
        setState(() {
          _gpsLat = lat;
          _gpsLng = lng;
          if (_locationController.text.trim().isEmpty) {
            _locationController.text = "GPS: ${lat.toStringAsFixed(5)}, ${lng.toStringAsFixed(5)}";
          }
        });
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text("📍 GPS Location captured successfully!"),
              backgroundColor: Color(0xFF15803D),
              duration: Duration(seconds: 2),
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Could not access GPS. Please type your Village / Locality / Landmark below."),
            duration: Duration(seconds: 3),
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isLocating = false);
      }
    }
  }

  Future<void> _handlePlaceOrder(CartProvider cart) async {
    final slNumber = _slNumberController.text.trim();
    final phone = _phoneController.text.trim();
    final location = _locationController.text.trim();
    final name = _nameController.text.trim();
    final notes = _notesController.text.trim();

    if (cart.items.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Your cart is empty")),
      );
      return;
    }

    if (phone.isEmpty || location.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text("Please fill WhatsApp Number and Current Location."),
          backgroundColor: Colors.redAccent,
        ),
      );
      return;
    }

    setState(() => _isPlacingOrder = true);

    try {
      final orderId = "order_${DateTime.now().millisecondsSinceEpoch}";
      final orderNumber = "1DD-${(1000 + (DateTime.now().microsecond % 9000))}";
      
      final double lat = _gpsLat ?? LocationConstants.storeOriginLat;
      final double lng = _gpsLng ?? LocationConstants.storeOriginLng;
      final double distance = (_gpsLat != null && _gpsLng != null)
          ? HaversineService.calculateDistanceFromStore(lat, lng)
          : 1.5;
      final int eta = HaversineService.estimateDeliveryMinutes(distance);

      final customerDisplayName = name.isNotEmpty
          ? (slNumber.isNotEmpty ? "$name (SL: $slNumber)" : name)
          : (slNumber.isNotEmpty ? "SL #$slNumber" : "Customer");
      final fullAddress = "${slNumber.isNotEmpty ? 'SL / House: $slNumber | ' : ''}Location: $location${_gpsLat != null ? ' [GPS: ${_gpsLat!.toStringAsFixed(4)}, ${_gpsLng!.toStringAsFixed(4)}]' : ''}";

      final storeOrder = StoreOrder(
        id: orderId,
        orderNumber: orderNumber,
        customerName: customerDisplayName,
        customerPhone: phone,
        deliveryAddress: fullAddress,
        deliveryLat: lat,
        deliveryLng: lng,
        landmarkName: location,
        items: cart.toOrderItems(),
        subtotal: cart.subtotal,
        deliveryFee: cart.deliveryFee,
        totalAmount: cart.totalAmount,
        status: OrderStatus.PLACED,
        orderTimestamp: DateTime.now().millisecondsSinceEpoch,
        deliveryPromise: "1-Day Express Local Delivery",
        deliveryNotes: notes,
        paymentMethod: "Direct Order (No Advance Payment)",
        paymentStatus: "Pay on Delivery (COD / UPI)",
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
        maxHeight: MediaQuery.of(context).size.height * 0.92,
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
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: const Color(0xFFDCFCE7),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(Icons.shopping_bag_outlined, color: Color(0xFF15803D), size: 22),
                  ),
                  const SizedBox(width: 10),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        "Your Order Cart (${cart.totalCount} items)",
                        style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 17),
                      ),
                      const Text(
                        "Quick Direct Order • No Payment Required",
                        style: TextStyle(fontSize: 12, color: Color(0xFF15803D), fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              )
            ],
          ),
          const Divider(height: 16),

          Flexible(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Cart Items List
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
                                width: 48,
                                height: 48,
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) => Container(
                                  width: 48,
                                  height: 48,
                                  color: Colors.grey.shade200,
                                  child: const Icon(Icons.image, size: 22, color: Colors.grey),
                                ),
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    item.product.title,
                                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                  Text(
                                    "₹${item.product.price.toStringAsFixed(0)} × ${item.quantity} = ₹${item.subtotal.toStringAsFixed(0)}",
                                    style: TextStyle(color: Colors.grey.shade700, fontSize: 12),
                                  ),
                                ],
                              ),
                            ),
                            Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.remove_circle_outline, size: 20),
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                                  onPressed: () => cart.decrementQuantity(item.product.id),
                                ),
                                Text(
                                  "${item.quantity}",
                                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.add_circle_outline, size: 20),
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                                  onPressed: () => cart.addToCart(item.product),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    }),

                  const Divider(height: 24),

                  // Customer Delivery Details Section
                  const Row(
                    children: [
                      Icon(Icons.location_on_outlined, size: 18, color: Color(0xFF15803D)),
                      SizedBox(width: 6),
                      Text(
                        "Delivery Details (গ্ৰাহকৰ ঠিকনা আৰু বিৱৰণ)",
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Current Location Field with GPS Button
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _locationController,
                          decoration: InputDecoration(
                            labelText: "Current Location / Village / Landmark *",
                            hintText: "e.g. Rangachakua / Balipara / Main Road",
                            prefixIcon: const Icon(Icons.pin_drop_outlined),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFFE0F2FE),
                          foregroundColor: const Color(0xFF0369A1),
                          elevation: 0,
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        onPressed: _isLocating ? null : _detectCurrentLocation,
                        icon: _isLocating
                            ? const SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(strokeWidth: 2),
                              )
                            : const Icon(Icons.my_location, size: 18),
                        label: Text(
                          _gpsLat != null ? "GPS Set ✓" : "Share GPS",
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 10),

                  // SL Number & WhatsApp Number Row
                  Row(
                    children: [
                      // SL / House Number (Optional)
                      Expanded(
                        child: TextField(
                          controller: _slNumberController,
                          decoration: InputDecoration(
                            labelText: "SL / House No. (Optional)",
                            hintText: "e.g. SL-12 / House 4",
                            prefixIcon: const Icon(Icons.tag),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                        ),
                      ),
                      const SizedBox(width: 10),
                      // WhatsApp Phone Number
                      Expanded(
                        child: TextField(
                          controller: _phoneController,
                          keyboardType: TextInputType.phone,
                          decoration: InputDecoration(
                            labelText: "WhatsApp No. *",
                            hintText: "8723811930",
                            prefixIcon: const Icon(Icons.phone_outlined),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 10),

                  // Customer Name (Optional) & Special Instructions
                  TextField(
                    controller: _nameController,
                    decoration: InputDecoration(
                      labelText: "Your Name (Optional)",
                      hintText: "e.g. Rahul Das",
                      prefixIcon: const Icon(Icons.person_outline),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),

                  const SizedBox(height: 10),

                  TextField(
                    controller: _notesController,
                    decoration: InputDecoration(
                      labelText: "Delivery Notes (Optional)",
                      hintText: "e.g. Near Kalibari mandir, call on arrival",
                      prefixIcon: const Icon(Icons.note_alt_outlined),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                  ),

                  const SizedBox(height: 14),

                  // Order Summary Card
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF1F5F9),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: const Color(0xFFCBD5E1)),
                    ),
                    child: Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text("Items Subtotal:", style: TextStyle(fontSize: 13, color: Colors.black87)),
                            Text("₹${cart.subtotal.toStringAsFixed(0)}", style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Row(
                              children: [
                                Icon(Icons.electric_bolt, size: 16, color: Color(0xFF15803D)),
                                SizedBox(width: 4),
                                Text("1-Day Express Delivery:", style: TextStyle(fontSize: 13, color: Colors.black87, fontWeight: FontWeight.w600)),
                              ],
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(
                                color: const Color(0xFFDCFCE7),
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: const Text(
                                "FREE (₹0)",
                                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Color(0xFF15803D)),
                              ),
                            ),
                          ],
                        ),
                        const Divider(height: 14),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text("Total Amount:", style: TextStyle(fontWeight: FontWeight.w900, fontSize: 14)),
                                Text("No Advance Payment Required", style: TextStyle(fontSize: 11, color: Color(0xFF15803D), fontWeight: FontWeight.w600)),
                              ],
                            ),
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

          const SizedBox(height: 14),

          // Direct Single Place Order Button
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF15803D), // Forest Green
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 14),
              elevation: 2,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: _isPlacingOrder || cart.items.isEmpty
                ? null
                : () => _handlePlaceOrder(cart),
            child: _isPlacingOrder
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2.5),
                  )
                : const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.check_circle_outline, size: 20),
                      SizedBox(width: 8),
                      Text(
                        "Place Order (অৰ্ডাৰ নিশ্চিত কৰক)",
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }
}

