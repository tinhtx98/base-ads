# Base Ads Consumer ProGuard Rules

# ============================================================================
# CRITICAL: AdMob Bidding Mediation Adapters
# These rules are REQUIRED for mediation adapters to work in release builds
# ============================================================================

# Keep all mediation adapter implementations
-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter { *; }
-keep class * implements com.google.android.gms.ads.mediation.Adapter { *; }
-keep class * implements com.google.android.gms.ads.mediation.rtb.RtbAdapter { *; }

# Keep adapter constructors (required for reflection-based loading)
-keepclassmembers class * implements com.google.android.gms.ads.mediation.MediationAdapter {
    public <init>();
}
-keepclassmembers class * implements com.google.android.gms.ads.mediation.Adapter {
    public <init>();
}
-keepclassmembers class * implements com.google.android.gms.ads.mediation.rtb.RtbAdapter {
    public <init>();
}

# ============================================================================
# AdMob Core SDK
# ============================================================================
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Keep OnPaidEventListener (required for eCPM tracking)
-keep class * implements com.google.android.gms.ads.OnPaidEventListener {
    <methods>;
}

# Keep AdListener callbacks
-keep class * extends com.google.android.gms.ads.AdListener {
    <methods>;
}

# ============================================================================
# Native Ad Support
# ============================================================================
-keep class com.google.android.gms.ads.nativead.** { *; }
-keep class com.google.android.gms.ads.nativead.NativeAd$* { *; }
-keepclassmembers class * implements com.google.android.gms.ads.nativead.NativeAd$OnNativeAdLoadedListener {
    <methods>;
}

# ============================================================================
# Open App Ad Support
# ============================================================================
-keep class com.google.android.gms.ads.appopen.** { *; }
-keep class com.google.android.gms.ads.appopen.AppOpenAd$* { *; }
-keepclassmembers class * implements com.google.android.gms.ads.appopen.AppOpenAd$AppOpenAdLoadCallback {
    <methods>;
}

# ProcessLifecycleOwner for Open App Ad
-keep class androidx.lifecycle.ProcessLifecycleOwner { *; }
-keep class * implements androidx.lifecycle.DefaultLifecycleObserver { *; }

# ============================================================================
# Vungle (Liftoff) Mediation Adapter
# ============================================================================
-keep class com.google.ads.mediation.vungle.** { *; }
-keep class com.vungle.** { *; }
-dontwarn com.vungle.**

# ============================================================================
# IronSource Mediation Adapter
# ============================================================================
-keep class com.google.ads.mediation.ironsource.** { *; }
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# IronSource SDK classes
-keep class com.ironsource.mediationsdk.** { *; }
-keep interface com.ironsource.mediationsdk.** { *; }

# IronSource adapter bridge classes
-keep class com.ironsource.adapters.** { *; }
-keep interface com.ironsource.adapters.** { *; }

# ============================================================================
# Meta Audience Network Mediation Adapter
# ============================================================================
-keep class com.google.ads.mediation.facebook.** { *; }
-keep class com.facebook.ads.** { *; }
-keepclassmembers class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**

# Meta SDK annotation classes (CRITICAL - prevents R8 errors)
-dontwarn com.facebook.infer.annotation.**
-keep class com.facebook.infer.annotation.** { *; }

# Meta SDK internal classes
-keep class com.facebook.internal.** { *; }
-dontwarn com.facebook.internal.**

# ============================================================================
# InMobi Mediation Adapter
# ============================================================================
-keep class com.google.ads.mediation.inmobi.** { *; }
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**

# InMobi SDK classes
-keep class com.inmobi.ads.** { *; }
-keep interface com.inmobi.ads.** { *; }

# InMobi internal classes
-keep class com.inmobi.media.** { *; }
-dontwarn com.inmobi.media.**

# ============================================================================
# Firebase Analytics (for ad event tracking)
# ============================================================================
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

# ============================================================================
# Hilt Dependency Injection
# ============================================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Provide* { *; }
-keep class **_Factory* { *; }

# ============================================================================
# Base Ads Library Public APIs
# ============================================================================
-keep public class com.tinhtx.baseads.** { 
    public *; 
}

# Keep BuildConfig for mediation partner names
-keep class com.tinhtx.baseads.library.BuildConfig { *; }