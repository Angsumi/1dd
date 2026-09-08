class LocationConstants {
  static const double storeOriginLat = 26.838775;
  static const double storeOriginLng = 92.910579;
  static const String storeHubName = "Store House";
  static const String storeHubAddress = "Central Store House, Rangachakua";
  static const String storePhone = "+91 87238 11930";
  static const double maxLocalDeliveryRadiusKm = 35.0;
  static const double baseDeliveryFee = 25.0;
}

class LocalLandmark {
  final String id;
  final String name;
  final String description;
  final double latitude;
  final double longitude;
  final String areaTag;

  const LocalLandmark({
    required this.id,
    required this.name,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.areaTag,
  });
}

const List<LocalLandmark> presetLocalDestinations = [
  LocalLandmark(
    id: "dest_1",
    name: "Mission Chariali Market",
    description: "Commercial Square, Near Bus Terminus",
    latitude: 26.848920,
    longitude: 92.924150,
    areaTag: "North Sector",
  ),
  LocalLandmark(
    id: "dest_2",
    name: "Lake View Colony, Block B",
    description: "Opposite Padum Pukhuri Park",
    latitude: 26.829140,
    longitude: 92.899450,
    areaTag: "South Lakeside",
  ),
  LocalLandmark(
    id: "dest_3",
    name: "University Road Campus Gate",
    description: "Napaam Campus Residential Zone",
    latitude: 26.852100,
    longitude: 92.841200,
    areaTag: "University Area",
  ),
  LocalLandmark(
    id: "dest_4",
    name: "Tribeni Commercial Complex",
    description: "Main MG Road, Shop #14",
    latitude: 26.835400,
    longitude: 92.915600,
    areaTag: "City Center",
  ),
  LocalLandmark(
    id: "dest_5",
    name: "Chowk Bazaar Corner",
    description: "Old Market Gate, Main Street",
    latitude: 26.840100,
    longitude: 92.918900,
    areaTag: "Central Market",
  ),
  LocalLandmark(
    id: "dest_6",
    name: "Green Heights Apartments",
    description: "Near Civil Hospital Road",
    latitude: 26.844500,
    longitude: 92.905200,
    areaTag: "East Sector",
  ),
];
