# Wear OS Watch Face service & renderer
-keep class * extends androidx.wear.watchface.WatchFaceService
-keep class watch.facesapp.wear.service.** { *; }
-keep class watch.facesapp.wear.renderer.** { *; }
-keep class watch.facesapp.wear.datalayer.** { *; }

# Google Play Services Wearable
-keep class com.google.android.gms.wearable.** { *; }

# AndroidX Wear
-keep class androidx.wear.** { *; }
