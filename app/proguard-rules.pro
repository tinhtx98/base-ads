# BaseAds Sample App ProGuard Rules

# Keep Google Mobile Ads SDK classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep ironSource SDK classes (app-level dependency)
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep OnPaidEventListener callbacks (CRITICAL for ILRD)
-keep class ** implements com.google.android.gms.ads.OnPaidEventListener { *; }
-keepclassmembers class ** {
    *** onPaidEvent(...);
}

# Keep AdListener callbacks
-keep class ** extends com.google.android.gms.ads.AdListener { *; }
-keep class ** extends com.google.android.gms.ads.FullScreenContentCallback { *; }

# Keep Base Ads public API
-keep public class com.tinhtx.baseads.** { 
    public *; 
}

# Keep Hilt generated classes
-keep class **_HiltModules* { *; }
-keep class **_Factory* { *; }
-keep class **_MembersInjector* { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile