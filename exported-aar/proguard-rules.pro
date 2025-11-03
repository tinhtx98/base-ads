# BaseAds Library ProGuard Rules

# Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ironSource
-keepclassmembers class com.ironsource.** { public *; }
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# Meta Audience Network (Facebook)
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# BaseAds
-keep class com.tinhtx.baseads.** { *; }
-dontwarn com.tinhtx.baseads.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
