import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';
import '../models/order.dart';
import '../models/product.dart';
import '../config/location_constants.dart';

class MetaCatalogService {
  static const String webStoreUrl = "https://1dd.web.app";
  static const String storeBrandName = "1DD Rangachakua";

  /// Generates official Meta Commerce Manager / WhatsApp Business Catalog CSV Feed.
  static String generateMetaCommerceCsv(List<Product> products) {
    final buffer = StringBuffer();
    // Standard Meta Commerce CSV Headers
    buffer.writeln("id,title,description,availability,condition,price,link,image_link,brand,google_product_category");

    for (final p in products) {
      final id = _escapeCsv(p.id);
      final title = _escapeCsv(p.title);
      final desc = _escapeCsv(p.description.isEmpty ? "${p.title} - Fresh local delivery by $storeBrandName" : p.description);
      final avail = (p.stockQuantity > 0 && p.isAvailable) ? "in stock" : "out of stock";
      const condition = "new";
      final price = "${p.price.toStringAsFixed(2)} INR";
      final link = _escapeCsv("$webStoreUrl?product=${p.id}");
      final imageLink = _escapeCsv(p.imageUrl);
      final brand = _escapeCsv(storeBrandName);
      const googleCat = "Food, Beverages & Tobacco > Food Items";

      buffer.writeln("$id,$title,$desc,$avail,$condition,$price,$link,$imageLink,$brand,$googleCat");
    }

    return buffer.toString();
  }

  /// Generates standard RSS 2.0 XML Data Feed for Meta Commerce Manager Scheduled Sync.
  static String generateMetaCommerceXml(List<Product> products) {
    final buffer = StringBuffer();
    buffer.writeln('<?xml version="1.0" encoding="UTF-8"?>');
    buffer.writeln('<rss xmlns:g="http://base.google.com/ns/1.0" version="2.0">');
    buffer.writeln('  <channel>');
    buffer.writeln('    <title>1DD Rangachakua WhatsApp Catalog</title>');
    buffer.writeln('    <link>$webStoreUrl</link>');
    buffer.writeln('    <description>Fresh Groceries &amp; Local Deliveries from Rangachakua Central Depot</description>');

    for (final p in products) {
      buffer.writeln('    <item>');
      buffer.writeln('      <g:id>${_xmlEscape(p.id)}</g:id>');
      buffer.writeln('      <g:title>${_xmlEscape(p.title)}</g:title>');
      buffer.writeln('      <g:description>${_xmlEscape(p.description.isEmpty ? p.title : p.description)}</g:description>');
      buffer.writeln('      <g:link>$webStoreUrl?product=${p.id}</g:link>');
      buffer.writeln('      <g:image_link>${_xmlEscape(p.imageUrl)}</g:image_link>');
      buffer.writeln('      <g:brand>$storeBrandName</g:brand>');
      buffer.writeln('      <g:condition>new</g:condition>');
      buffer.writeln('      <g:availability>${(p.stockQuantity > 0 && p.isAvailable) ? "in stock" : "out of stock"}</g:availability>');
      buffer.writeln('      <g:price>${p.price.toStringAsFixed(2)} INR</g:price>');
      buffer.writeln('    </item>');
    }

    buffer.writeln('  </channel>');
    buffer.writeln('</rss>');

    return buffer.toString();
  }

  /// Directly pushes or updates a product into Meta Commerce Catalog via Meta Graph API.
  static Future<Map<String, dynamic>> pushProductToMetaGraphApi({
    required Product product,
    required String catalogId,
    required String accessToken,
  }) async {
    if (catalogId.isEmpty || accessToken.isEmpty) {
      return {
        "success": false,
        "message": "Meta Catalog ID or Access Token is missing.",
      };
    }

    try {
      final url = Uri.parse("https://graph.facebook.com/v20.0/$catalogId/items_batch");
      final requestBody = {
        "access_token": accessToken,
        "item_type": "PRODUCT_ITEM",
        "requests": [
          {
            "method": "UPDATE",
            "retailer_id": product.id,
            "data": {
              "name": product.title,
              "description": product.description.isNotEmpty ? product.description : product.title,
              "availability": (product.stockQuantity > 0 && product.isAvailable) ? "in stock" : "out of stock",
              "condition": "new",
              "price": (product.price * 100).toInt(),
              "currency": "INR",
              "url": "$webStoreUrl?product=${product.id}",
              "image_url": product.imageUrl,
              "brand": storeBrandName,
              "category": product.category.name,
            }
          }
        ]
      };

      final response = await http.post(
        url,
        headers: {"Content-Type": "application/json"},
        body: jsonEncode(requestBody),
      );

      final decoded = jsonDecode(response.body);
      if (response.statusCode == 200) {
        return {
          "success": true,
          "message": "Successfully synced to Meta Catalog!",
          "data": decoded,
        };
      } else {
        return {
          "success": false,
          "message": decoded['error']?['message'] ?? "Failed to sync to Meta Graph API (${response.statusCode})",
          "error": decoded,
        };
      }
    } catch (e) {
      return {
        "success": false,
        "message": "Network error connecting to Meta: $e",
      };
    }
  }

  /// Formats a complete customer order message for WhatsApp dispatch.
  static String formatWhatsAppOrderMessage(StoreOrder order) {
    final buffer = StringBuffer();
    buffer.writeln("🛍️ *NEW 1DD ORDER ${order.orderNumber}*");
    buffer.writeln("━━━━━━━━━━━━━━━━━━");
    buffer.writeln("👤 *Customer:* ${order.customerName}");
    buffer.writeln("📞 *Phone:* ${order.customerPhone}");
    buffer.writeln("📍 *Delivery Location:* ${order.deliveryAddress}");
    if (order.landmarkName.isNotEmpty) {
      buffer.writeln("📌 *Landmark:* ${order.landmarkName}");
    }
    buffer.writeln("⏱️ *Haversine Distance:* ${order.distanceKm.toStringAsFixed(2)} km (~${order.estimatedMinutes} mins)");
    buffer.writeln("");
    buffer.writeln("📦 *ITEMS ORDERED:*");
    for (final item in order.items) {
      buffer.writeln("• ${item.productTitle} × ${item.quantity} (${item.unit}) = ₹${item.subtotal.toStringAsFixed(0)}");
    }
    buffer.writeln("━━━━━━━━━━━━━━━━━━");
    buffer.writeln("Subtotal: ₹${order.subtotal.toStringAsFixed(0)}");
    buffer.writeln("1-Day Express Delivery: ₹${order.deliveryFee.toStringAsFixed(0)}");
    buffer.writeln("💰 *TOTAL PAYABLE:* ₹${order.totalAmount.toStringAsFixed(0)}");
    buffer.writeln("💳 *Payment Mode:* ${order.paymentMethod}");
    buffer.writeln("🔐 *Delivery OTP Code:* ${order.otpCode}");
    if (order.deliveryNotes.isNotEmpty) {
      buffer.writeln("📝 *Notes:* ${order.deliveryNotes}");
    }
    buffer.writeln("");
    buffer.writeln("⚡ *1DD Express Local Delivery Guarantee*");
    buffer.writeln("🌐 Store Link: $webStoreUrl");

    return buffer.toString();
  }

  /// Generates product share message for WhatsApp.
  static String formatProductWhatsAppShareText(Product product) {
    return "🛒 *Check out ${product.title} on 1DD Rangachakua!*\n"
        "💰 Price: ₹${product.price.toStringAsFixed(0)} / ${product.unit}\n"
        "⚡ 1-Day Express Local Delivery\n\n"
        "👉 Order now: $webStoreUrl?product=${product.id}";
  }

  /// Generates order status update message for Seller to send to Customer.
  static String formatSellerStatusUpdateMessage(StoreOrder order, OrderStatus newStatus) {
    final buffer = StringBuffer();
    buffer.writeln("👋 Hello *${order.customerName}*,");
    buffer.writeln("");

    switch (newStatus) {
      case OrderStatus.PLACED:
        buffer.writeln("✅ Your order *${order.orderNumber}* has been received and confirmed!");
        break;
      case OrderStatus.PREPARING:
        buffer.writeln("📦 Your order *${order.orderNumber}* is now being packed at Rangachakua Central Depot.");
        break;
      case OrderStatus.OUT_FOR_DELIVERY:
        buffer.writeln("🚚 Good news! Your order *${order.orderNumber}* is *OUT FOR DELIVERY*.");
        buffer.writeln("⏱️ Estimated Arrival: ~${order.estimatedMinutes} minutes.");
        buffer.writeln("🔐 Delivery OTP: *${order.otpCode}* (Share with rider upon arrival).");
        break;
      case OrderStatus.DELIVERED:
        buffer.writeln("🎉 Order *${order.orderNumber}* has been *DELIVERED*!");
        buffer.writeln("Thank you for shopping local with 1DD Rangachakua Store!");
        break;
      case OrderStatus.CANCELLED:
        buffer.writeln("⚠️ Order *${order.orderNumber}* has been cancelled. Contact support for details.");
        break;
    }

    buffer.writeln("");
    buffer.writeln("💰 Total Amount: ₹${order.totalAmount.toStringAsFixed(0)} (${order.paymentMethod})");
    buffer.writeln("📞 Store Hotline: ${LocationConstants.storePhone}");

    return buffer.toString();
  }

  /// Launches WhatsApp web/app with pre-filled text.
  static Future<bool> launchWhatsApp({
    required String phone,
    required String message,
  }) async {
    // Sanitize phone number (strip spaces, dashes, parentheses)
    String cleanPhone = phone.replaceAll(RegExp(r'[^0-9+]'), '');
    if (cleanPhone.startsWith('+')) {
      cleanPhone = cleanPhone.substring(1);
    }
    // Default to India +91 if 10 digits
    if (cleanPhone.length == 10) {
      cleanPhone = "91$cleanPhone";
    }

    final encodedMsg = Uri.encodeComponent(message);
    final url = cleanPhone.isNotEmpty
        ? "https://wa.me/$cleanPhone?text=$encodedMsg"
        : "https://wa.me/?text=$encodedMsg";

    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      return await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
    return false;
  }

  static String _escapeCsv(String text) {
    if (text.contains(',') || text.contains('"') || text.contains('\n')) {
      return '"${text.replaceAll('"', '""')}"';
    }
    return text;
  }

  static String _xmlEscape(String text) {
    return text
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&apos;');
  }
}
