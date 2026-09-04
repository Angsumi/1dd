import 'dart:math';
import '../config/location_constants.dart';

class HaversineService {
  static const double earthRadiusKm = 6371.0;

  static double calculateDistanceKm(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) {
    final double lat1Rad = lat1 * pi / 180.0;
    final double lat2Rad = lat2 * pi / 180.0;
    final double dLat = (lat2 - lat1) * pi / 180.0;
    final double dLon = (lon2 - lon1) * pi / 180.0;

    final double a = sin(dLat / 2) * sin(dLat / 2) +
        cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2) * sin(dLon / 2);
    final double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    final double distance = earthRadiusKm * c;

    return (distance * 100).round() / 100.0;
  }

  static double calculateDistanceFromStore(double targetLat, double targetLng) {
    return calculateDistanceKm(
      LocationConstants.storeOriginLat,
      LocationConstants.storeOriginLng,
      targetLat,
      targetLng,
    );
  }

  static int estimateDeliveryMinutes(double distanceKm) {
    return max(20, min(180, (15 + (distanceKm * 3.5)).round()));
  }

  static String formatDistance(double distanceKm) {
    if (distanceKm < 1.0) {
      return "${(distanceKm * 1000).toInt()} m";
    }
    return "${distanceKm.toStringAsFixed(1)} km";
  }
}
