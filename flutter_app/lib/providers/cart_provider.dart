import 'package:flutter/foundation.dart';
import '../config/location_constants.dart';
import '../models/product.dart';
import '../models/order.dart';
import '../services/haversine_service.dart';

class CartItem {
  final Product product;
  int quantity;

  CartItem({required this.product, this.quantity = 1});

  double get subtotal => product.price * quantity;
}

class CartProvider extends ChangeNotifier {
  final Map<String, CartItem> _items = {};
  LocalLandmark _selectedLandmark = presetLocalDestinations[0];

  Map<String, CartItem> get items => {..._items};
  LocalLandmark get selectedLandmark => _selectedLandmark;

  int get totalCount {
    return _items.values.fold(0, (sum, it) => sum + it.quantity);
  }

  double get subtotal {
    return _items.values.fold(0.0, (sum, it) => sum + it.subtotal);
  }

  double get deliveryFee => 25.0;

  double get totalAmount => subtotal + deliveryFee;

  double get deliveryDistanceKm {
    return HaversineService.calculateDistanceFromStore(
      _selectedLandmark.latitude,
      _selectedLandmark.longitude,
    );
  }

  int get estimatedDeliveryMinutes {
    return HaversineService.estimateDeliveryMinutes(deliveryDistanceKm);
  }

  void setLandmark(LocalLandmark landmark) {
    _selectedLandmark = landmark;
    notifyListeners();
  }

  void addToCart(Product product) {
    if (_items.containsKey(product.id)) {
      if (_items[product.id]!.quantity < product.stockQuantity) {
        _items[product.id]!.quantity++;
      }
    } else {
      if (product.stockQuantity > 0) {
        _items[product.id] = CartItem(product: product, quantity: 1);
      }
    }
    notifyListeners();
  }

  void decrementQuantity(String productId) {
    if (!_items.containsKey(productId)) return;
    if (_items[productId]!.quantity > 1) {
      _items[productId]!.quantity--;
    } else {
      _items.remove(productId);
    }
    notifyListeners();
  }

  void removeProduct(String productId) {
    _items.remove(productId);
    notifyListeners();
  }

  void clear() {
    _items.clear();
    notifyListeners();
  }

  List<OrderItem> toOrderItems() {
    return _items.values.map((ci) {
      return OrderItem(
        productId: ci.product.id,
        productTitle: ci.product.title,
        unitPrice: ci.product.price,
        quantity: ci.quantity,
        unit: ci.product.unit,
        imageUrl: ci.product.imageUrl,
      );
    }).toList();
  }
}
