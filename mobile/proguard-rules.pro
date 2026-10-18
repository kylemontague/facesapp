# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-keep class watch.facesapp.mobile.data.db.** { *; }
-dontwarn androidx.room.paging.**

# CameraX & Palette
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Google Play Services Wearable
-keep class com.google.android.gms.wearable.** { *; }

# ZXing QR Code
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
