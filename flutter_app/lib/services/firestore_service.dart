import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/product.dart';
import '../models/order.dart';

class FirestoreService {
  final FirebaseFirestore _db = FirebaseFirestore.instance;

  // Stream Products Collection
  Stream<List<Product>> getProductsStream() {
    return _db
        .collection('store_products')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs.map((doc) {
        return Product.fromMap(doc.id, doc.data());
      }).toList();
    });
  }

  // Stream Orders Collection
  Stream<List<StoreOrder>> getOrdersStream() {
    return _db
        .collection('store_orders')
        .snapshots()
        .map((snapshot) {
      final orders = snapshot.docs.map((doc) {
        return StoreOrder.fromMap(doc.id, doc.data());
      }).toList();
      orders.sort((a, b) => b.orderTimestamp.compareTo(a.orderTimestamp));
      return orders;
    });
  }

  // Stream Single Order by ID
  Stream<StoreOrder?> getOrderStream(String orderId) {
    return _db
        .collection('store_orders')
        .doc(orderId)
        .snapshots()
        .map((doc) {
      if (!doc.exists || doc.data() == null) return null;
      return StoreOrder.fromMap(doc.id, doc.data()!);
    });
  }

  // Create Product
  Future<void> createProduct(Product product) async {
    await _db
        .collection('store_products')
        .doc(product.id)
        .set(product.toMap(), SetOptions(merge: true));
  }

  // Update Product Stock
  Future<void> updateProductStock(String productId, int newStock) async {
    await _db.collection('store_products').doc(productId).update({
      'stockQuantity': newStock,
      'isAvailable': newStock > 0,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
    });
  }

  // Delete Product
  Future<void> deleteProduct(String productId) async {
    await _db.collection('store_products').doc(productId).delete();
  }

  // Place Order & Decrement Stock atomically
  Future<String> placeOrder(StoreOrder order) async {
    final batch = _db.batch();

    // 1. Write order document
    final orderDoc = _db.collection('store_orders').doc(order.id);
    batch.set(orderDoc, order.toMap());

    // 2. Decrement stock for each item
    for (final item in order.items) {
      final productDoc = _db.collection('store_products').doc(item.productId);
      batch.update(productDoc, {
        'stockQuantity': FieldValue.increment(-item.quantity),
        'updatedAt': DateTime.now().millisecondsSinceEpoch,
      });
    }

    await batch.commit();
    return order.id;
  }

  // Update Order Status
  Future<void> updateOrderStatus(String orderId, OrderStatus newStatus) async {
    await _db.collection('store_orders').doc(orderId).update({
      'status': newStatus.name,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
    });
  }

  // Delete Order
  Future<void> deleteOrder(String orderId) async {
    await _db.collection('store_orders').doc(orderId).delete();
  }
}
