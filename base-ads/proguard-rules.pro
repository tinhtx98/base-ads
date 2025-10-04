# Base Ads Module ProGuard Rules

# Keep Google Mobile Ads SDK classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.mediation.**

# Keep Vungle (Liftoff) SDK classes
-keep class com.vungle.** { *; }
-keep class com.liftoff.** { *; }
-dontwarn com.vungle.**
-dontwarn com.liftoff.**

# Keep ironSource SDK classes
-keep class com.ironsource.** { *; }
-keep class com.unity3d.** { *; }
-dontwarn com.ironsource.**
-dontwarn com.unity3d.**

# Keep Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.ktx.** { *; }
-dontwarn com.google.firebase.**

# Keep Hilt/Dagger generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Provide* { *; }
-keep class **_Factory* { *; }
-keep class **_MembersInjector* { *; }

# Keep Base Ads Module public APIs
-keep public class com.tinhtx.baseads.** { 
    public *; 
}

# Keep OnPaidEventListener for ILRD (Important!)
-keep class ** implements com.google.android.gms.ads.OnPaidEventListener { *; }
-keepclassmembers class ** {
    *** onPaidEvent(...);
}

# Keep AdListener callbacks
-keep class ** extends com.google.android.gms.ads.AdListener { *; }
-keep class ** extends com.google.android.gms.ads.FullScreenContentCallback { *; }

# Keep mediation adapter configurations
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep reflection-based mediation calls
-keepclassmembers class ** {
    @com.google.android.gms.ads.mediation.** *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile