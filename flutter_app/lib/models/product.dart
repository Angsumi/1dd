enum ProductCategory {
  ALL,
  VEGETABLES,
  LOCAL_SPECIALS,
  DAIRY_BAKERY,
  GRAINS_SPICES,
}

extension ProductCategoryExt on ProductCategory {
  String get displayName {
    switch (this) {
      case ProductCategory.ALL:
        return "All Items";
      case ProductCategory.VEGETABLES:
        return "Vegetables";
      case ProductCategory.LOCAL_SPECIALS:
        return "Local Specials";
      case ProductCategory.DAIRY_BAKERY:
        return "Dairy & Bakery";
      case ProductCategory.GRAINS_SPICES:
        return "Grains & Spices";
    }
  }

  String get iconEmoji {
    switch (this) {
      case ProductCategory.ALL:
        return "🛒";
      case ProductCategory.VEGETABLES:
        return "🥦";
      case ProductCategory.LOCAL_SPECIALS:
        return "⭐";
      case ProductCategory.DAIRY_BAKERY:
        return "🥛";
      case ProductCategory.GRAINS_SPICES:
        return "🌾";
    }
  }
}

class Product {
  final String id;
  final String title;
  final String description;
  final double price;
  final String unit;
  final ProductCategory category;
  final int stockQuantity;
  final String imageUrl;
  final bool isLocalSpecialty;
  final bool isOneDayDelivery;
  final double rating;
  final int reviewCount;
  final String sellerName;
  final bool isAvailable;

  const Product({
    required this.id,
    required this.title,
    required this.description,
    required this.price,
    required this.unit,
    required this.category,
    required this.stockQuantity,
    required this.imageUrl,
    this.isLocalSpecialty = true,
    this.isOneDayDelivery = true,
    this.rating = 5.0,
    this.reviewCount = 0,
    this.sellerName = "Store House Owner",
    this.isAvailable = true,
  });

  factory Product.fromMap(String id, Map<String, dynamic> map) {
    ProductCategory cat = ProductCategory.VEGETABLES;
    try {
      cat = ProductCategory.values.firstWhere(
        (e) => e.name == (map['category'] as String? ?? ''),
        orElse: () => ProductCategory.VEGETABLES,
      );
    } catch (_) {}

    return Product(
      id: id,
      title: map['title'] as String? ?? 'Product',
      description: map['description'] as String? ?? '',
      price: (map['price'] as num?)?.toDouble() ?? 0.0,
      unit: map['unit'] as String? ?? '1 unit',
      category: cat,
      stockQuantity: (map['stockQuantity'] as num?)?.toInt() ?? 0,
      imageUrl: map['imageUrl'] as String? ?? '',
      isLocalSpecialty: map['isLocalSpecialty'] as bool? ?? true,
      isOneDayDelivery: map['isOneDayDelivery'] as bool? ?? true,
      rating: (map['rating'] as num?)?.toDouble() ?? 5.0,
      reviewCount: (map['reviewCount'] as num?)?.toInt() ?? 0,
      sellerName: map['sellerName'] as String? ?? 'Store House Owner',
      isAvailable: map['isAvailable'] as bool? ?? true,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'title': title,
      'description': description,
      'price': price,
      'unit': unit,
      'category': category.name,
      'stockQuantity': stockQuantity,
      'imageUrl': imageUrl,
      'isLocalSpecialty': isLocalSpecialty,
      'isOneDayDelivery': isOneDayDelivery,
      'rating': rating,
      'reviewCount': reviewCount,
      'sellerName': sellerName,
      'isAvailable': isAvailable,
      'updatedAt': DateTime.now().millisecondsSinceEpoch,
    };
  }

  Product copyWith({
    String? title,
    String? description,
    double? price,
    String? unit,
    ProductCategory? category,
    int? stockQuantity,
    String? imageUrl,
    bool? isAvailable,
  }) {
    return Product(
      id: id,
      title: title ?? this.title,
      description: description ?? this.description,
      price: price ?? this.price,
      unit: unit ?? this.unit,
      category: category ?? this.category,
      stockQuantity: stockQuantity ?? this.stockQuantity,
      imageUrl: imageUrl ?? this.imageUrl,
      isLocalSpecialty: isLocalSpecialty,
      isOneDayDelivery: isOneDayDelivery,
      rating: rating,
      reviewCount: reviewCount,
      sellerName: sellerName,
      isAvailable: isAvailable ?? this.isAvailable,
    );
  }
}
