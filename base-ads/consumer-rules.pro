# Base Ads Consumer ProGuard Rules
# Keep AdMob and ironSource classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# Keep Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Provide* { *; }
-keep class **_Factory* { *; }

# Keep our public APIs
-keep public class com.tinhtx.baseads.** { 
    public *; 
}