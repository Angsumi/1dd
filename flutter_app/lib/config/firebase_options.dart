import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      default:
        return web;
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyCse6chuBqg-5MLIo957BtjFQLWha_KaD8',
    appId: '1:927542412178:web:760d4874ee8c6a8481f74b',
    messagingSenderId: '927542412178',
    projectId: 'onedaydelivery-market',
    authDomain: 'onedaydelivery-market.firebaseapp.com',
    storageBucket: 'onedaydelivery-market.firebasestorage.app',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyCse6chuBqg-5MLIo957BtjFQLWha_KaD8',
    appId: '1:927542412178:android:760d4874ee8c6a8481f74b',
    messagingSenderId: '927542412178',
    projectId: 'onedaydelivery-market',
    storageBucket: 'onedaydelivery-market.firebasestorage.app',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'AIzaSyCse6chuBqg-5MLIo957BtjFQLWha_KaD8',
    appId: '1:927542412178:ios:760d4874ee8c6a8481f74b',
    messagingSenderId: '927542412178',
    projectId: 'onedaydelivery-market',
    storageBucket: 'onedaydelivery-market.firebasestorage.app',
  );
}
