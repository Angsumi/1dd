import 'package:flutter/material.dart';
import '../models/order.dart';
import '../services/firestore_service.dart';

class OrderTrackerDialog extends StatelessWidget {
  final String orderId;

  const OrderTrackerDialog({super.key, required this.orderId});

  @override
  Widget build(BuildContext context) {
    final firestore = FirestoreService();

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 500),
        padding: const EdgeInsets.all(24),
        child: StreamBuilder<StoreOrder?>(
          stream: firestore.getOrderStream(orderId),
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return const Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text("Connecting to live order feed..."),
                ],
              );
            }

            final order = snapshot.data;
            if (order == null) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.error_outline, size: 48, color: Colors.red),
                  const SizedBox(height: 12),
                  const Text("Order details not found."),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text("Close"),
                  )
                ],
              );
            }

            final steps = [
              {"status": OrderStatus.PLACED, "title": "1. Order Placed", "subtitle": "Received at Store House Depot"},
              {"status": OrderStatus.PREPARING, "title": "2. Packed at Depot", "subtitle": "Freshness verified & sanitized"},
              {"status": OrderStatus.OUT_FOR_DELIVERY, "title": "3. Out for 1-Day Delivery", "subtitle": "Dispatched on local route"},
              {"status": OrderStatus.DELIVERED, "title": "4. Delivered", "subtitle": "Order handover completed"},
            ];

            final currentIdx = steps.indexWhere((s) => s['status'] == order.status);

            return SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: const Color(0xFFDCFCE7),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: const Icon(Icons.local_shipping, color: Color(0xFF15803D)),
                          ),
                          const SizedBox(width: 12),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                order.orderNumber,
                                style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18),
                              ),
                              const Text(
                                "1-Day Express Local Delivery",
                                style: TextStyle(color: Colors.grey, fontSize: 12),
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
                  const SizedBox(height: 20),

                  // Progress Step Tracker
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: const Color(0xFFE2E8F0)),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          "Live Dispatch Progression",
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                        const SizedBox(height: 16),
                        ...steps.asMap().entries.map((entry) {
                          final idx = entry.key;
                          final step = entry.value;
                          final isDone = currentIdx != -1 && idx <= currentIdx;
                          final isCurrent = idx == currentIdx;

                          return Padding(
                            padding: const EdgeInsets.only(bottom: 12),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  width: 24,
                                  height: 24,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: isDone ? const Color(0xFF15803D) : const Color(0xFFE2E8F0),
                                  ),
                                  child: Center(
                                    child: isDone
                                        ? const Icon(Icons.check, size: 14, color: Colors.white)
                                        : Text("${idx + 1}", style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.black54)),
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        step['title'] as String,
                                        style: TextStyle(
                                          fontWeight: isCurrent ? FontWeight.w900 : FontWeight.w600,
                                          fontSize: 14,
                                          color: isCurrent ? const Color(0xFF15803D) : Colors.black87,
                                        ),
                                      ),
                                      Text(
                                        step['subtitle'] as String,
                                        style: const TextStyle(fontSize: 12, color: Colors.grey),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          );
                        }),
                      ],
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Order Summary Card
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: const Color(0xFFE2E8F0)),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text("Delivery OTP Code:", style: TextStyle(fontWeight: FontWeight.bold)),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: const Color(0xFFFEF3C7),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                order.otpCode,
                                style: const TextStyle(
                                  fontFamily: 'monospace',
                                  fontSize: 18,
                                  fontWeight: FontWeight.w900,
                                  color: Color(0xFF92400E),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const Divider(height: 20),
                        Text("📍 Destination: ${order.deliveryAddress}", style: const TextStyle(fontSize: 13)),
                        const SizedBox(height: 4),
                        Text("⏱️ Haversine Distance: ${order.distanceKm} km (~${order.estimatedMinutes} mins)", style: const TextStyle(fontSize: 13)),
                        const SizedBox(height: 4),
                        Text("💵 Total Amount: ₹${order.totalAmount.toStringAsFixed(0)} (${order.paymentMethod})", style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: Color(0xFF15803D))),
                      ],
                    ),
                  ),

                  const SizedBox(height: 20),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF15803D),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    onPressed: () => Navigator.pop(context),
                    child: const Text("Done / Keep Shopping", style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
