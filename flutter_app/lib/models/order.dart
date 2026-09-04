import 'dart:convert';

enum OrderStatus {
  PLACED,
  PREPARING,
  OUT_FOR_DELIVERY,
  DELIVERED,
  CANCELLED,
}

extension OrderStatusExt on OrderStatus {
  String get displayName {
    switch (this) {
      case OrderStatus.PLACED:
        return "1. Order Placed";
      case OrderStatus.PREPARING:
        return "2. Packed at Depot";
      case OrderStatus.OUT_FOR_DELIVERY:
        return "3. Out for Delivery";
      case OrderStatus.DELIVERED:
        return "4. Delivered";
      case OrderStatus.CANCELLED:
        return "Cancelled";
    }
  }

  String get shortLabel {
    switch (this) {
      case OrderStatus.PLACED:
        return "Placed";
      case OrderStatus.PREPARING:
        return "Preparing";
      case OrderStatus.OUT_FOR_DELIVERY:
        return "Out for Delivery";
      case OrderStatus.DELIVERED:
        return "Delivered";
      case OrderStatus.CANCELLED:
        return "Cancelled";
    }
  }
}

class OrderItem {
  final String productId;
  final String productTitle;
  final double unitPrice;
  final int quantity;
  final String unit;
  final String imageUrl;

  const OrderItem({
    required this.productId,
    required this.productTitle,
    required this.unitPrice,
    required this.quantity,
    required this.unit,
    required this.imageUrl,
  });

  double get subtotal => unitPrice * quantity;

  factory OrderItem.fromMap(Map<String, dynamic> map) {
    return OrderItem(
      productId: map['productId'] as String? ?? '',
      productTitle: map['productTitle'] as String? ?? 'Item',
      unitPrice: (map['unitPrice'] as num?)?.toDouble() ?? 0.0,
      quantity: (map['quantity'] as num?)?.toInt() ?? 1,
      unit: map['unit'] as String? ?? 'unit',
      imageUrl: map['imageUrl'] as String? ?? '',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'productId': productId,
      'productTitle': productTitle,
      'unitPrice': unitPrice,
      'quantity': quantity,
      'unit': unit,
      'imageUrl': imageUrl,
    };
  }
}

class StoreOrder {
  final String id;
  final String orderNumber;
  final String customerName;
  final String customerPhone;
  final String deliveryAddress;
  final double deliveryLat;
  final double deliveryLng;
  final String landmarkName;
  final List<OrderItem> items;
  final double subtotal;
  final double deliveryFee;
  final double totalAmount;
  final OrderStatus status;
  final int orderTimestamp;
  final String deliveryPromise;
  final String deliveryNotes;
  final String paymentMethod;
  final String paymentStatus;
  final double distanceKm;
  final int estimatedMinutes;
  final String deliveryPartnerName;
  final String deliveryPartnerPhone;
  final String otpCode;

  const StoreOrder({
    required this.id,
    required this.orderNumber,
    required this.customerName,
    required this.customerPhone,
    required this.deliveryAddress,
    required this.deliveryLat,
    required this.deliveryLng,
    required this.landmarkName,
    required this.items,
    required this.subtotal,
    required this.deliveryFee,
    required this.totalAmount,
    required this.status,
    required this.orderTimestamp,
    this.deliveryPromise = "1-Day Express Local Delivery",
    this.deliveryNotes = "",
    this.paymentMethod = "Cash on Delivery",
    this.paymentStatus = "Pending on Delivery",
    required this.distanceKm,
    required this.estimatedMinutes,
    this.deliveryPartnerName = "Store House Partner",
    this.deliveryPartnerPhone = "+91 98640 12345",
    required this.otpCode,
  });

  factory StoreOrder.fromMap(String id, Map<String, dynamic> map) {
    List<OrderItem> parsedItems = [];
    if (map['itemsJson'] != null) {
      try {
        final decoded = jsonDecode(map['itemsJson'] as String);
        if (decoded is List) {
          parsedItems = decoded
              .map((it) => OrderItem.fromMap(it as Map<String, dynamic>))
              .toList();
        }
      } catch (_) {}
    } else if (map['items'] is List) {
      parsedItems = (map['items'] as List)
          .map((it) => OrderItem.fromMap(it as Map<String, dynamic>))
          .toList();
    }

    OrderStatus st = OrderStatus.PLACED;
    try {
      st = OrderStatus.values.firstWhere(
        (e) => e.name == (map['status'] as String? ?? ''),
        orElse: () => OrderStatus.PLACED,
      );
    } catch (_) {}

    return StoreOrder(
      id: id,
      orderNumber: map['orderNumber'] as String? ?? '1DD-${id.substring(id.length - 4)}',
      customerName: map['customerName'] as String? ?? 'Customer',
      customerPhone: map['customerPhone'] as String? ?? '',
      deliveryAddress: map['deliveryAddress'] as String? ?? '',
      deliveryLat: (map['deliveryLat'] as num?)?.toDouble() ?? 0.0,
      deliveryLng: (map['deliveryLng'] as num?)?.toDouble() ?? 0.0,
      landmarkName: map['landmarkName'] as String? ?? '',
      items: parsedItems,
      subtotal: (map['subtotal'] as num?)?.toDouble() ?? 0.0,
      deliveryFee: (map['deliveryFee'] as num?)?.toDouble() ?? 25.0,
      totalAmount: (map['totalAmount'] as num?)?.toDouble() ?? 0.0,
      status: st,
      orderTimestamp: (map['orderTimestamp'] as num?)?.toInt() ?? DateTime.now().millisecondsSinceEpoch,
      deliveryPromise: map['deliveryPromise'] as String? ?? "1-Day Express Local Delivery",
      deliveryNotes: map['deliveryNotes'] as String? ?? "",
      paymentMethod: map['paymentMethod'] as String? ?? "Cash on Delivery",
      paymentStatus: map['paymentStatus'] as String? ?? "Pending on Delivery",
      distanceKm: (map['distanceKm'] as num?)?.toDouble() ?? 0.0,
      estimatedMinutes: (map['estimatedMinutes'] as num?)?.toInt() ?? 30,
      deliveryPartnerName: map['deliveryPartnerName'] as String? ?? "Store House Partner",
      deliveryPartnerPhone: map['deliveryPartnerPhone'] as String? ?? "+91 98640 12345",
      otpCode: map['otpCode'] as String? ?? "1234",
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'orderNumber': orderNumber,
      'customerName': customerName,
      'customerPhone': customerPhone,
      'deliveryAddress': deliveryAddress,
      'deliveryLat': deliveryLat,
      'deliveryLng': deliveryLng,
      'landmarkName': landmarkName,
      'itemsJson': jsonEncode(items.map((e) => e.toMap()).toList()),
      'subtotal': subtotal,
      'deliveryFee': deliveryFee,
      'totalAmount': totalAmount,
      'status': status.name,
      'orderTimestamp': orderTimestamp,
      'deliveryPromise': deliveryPromise,
      'deliveryNotes': deliveryNotes,
      'paymentMethod': paymentMethod,
      'paymentStatus': paymentStatus,
      'distanceKm': distanceKm,
      'estimatedMinutes': estimatedMinutes,
      'deliveryPartnerName': deliveryPartnerName,
      'deliveryPartnerPhone': deliveryPartnerPhone,
      'otpCode': otpCode,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
    };
  }
}
