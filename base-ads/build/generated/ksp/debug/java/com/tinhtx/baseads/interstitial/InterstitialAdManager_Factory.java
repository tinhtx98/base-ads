package com.tinhtx.baseads.interstitial;

import android.content.Context;
import com.tinhtx.baseads.core.AdUnitsProvider;
import com.tinhtx.baseads.core.AdsConfig;
import com.tinhtx.baseads.core.AnalyticsLogger;
import com.tinhtx.baseads.core.VipGate;
import com.tinhtx.baseads.data.AdsPrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class InterstitialAdManager_Factory implements Factory<InterstitialAdManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AdUnitsProvider> adUnitsProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  private final Provider<VipGate> vipGateProvider;

  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  private final Provider<AdsPrefs> adsPrefsProvider;

  public InterstitialAdManager_Factory(Provider<Context> contextProvider,
      Provider<AdUnitsProvider> adUnitsProvider, Provider<AdsConfig> adsConfigProvider,
      Provider<VipGate> vipGateProvider, Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsPrefs> adsPrefsProvider) {
    this.contextProvider = contextProvider;
    this.adUnitsProvider = adUnitsProvider;
    this.adsConfigProvider = adsConfigProvider;
    this.vipGateProvider = vipGateProvider;
    this.analyticsLoggerProvider = analyticsLoggerProvider;
    this.adsPrefsProvider = adsPrefsProvider;
  }

  @Override
  public InterstitialAdManager get() {
    return newInstance(contextProvider.get(), adUnitsProvider.get(), adsConfigProvider.get(), vipGateProvider.get(), analyticsLoggerProvider.get(), adsPrefsProvider.get());
  }

  public static InterstitialAdManager_Factory create(Provider<Context> contextProvider,
      Provider<AdUnitsProvider> adUnitsProvider, Provider<AdsConfig> adsConfigProvider,
      Provider<VipGate> vipGateProvider, Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsPrefs> adsPrefsProvider) {
    return new InterstitialAdManager_Factory(contextProvider, adUnitsProvider, adsConfigProvider, vipGateProvider, analyticsLoggerProvider, adsPrefsProvider);
  }

  public static InterstitialAdManager newInstance(Context context, AdUnitsProvider adUnitsProvider,
      AdsConfig adsConfig, VipGate vipGate, AnalyticsLogger analyticsLogger, AdsPrefs adsPrefs) {
    return new InterstitialAdManager(context, adUnitsProvider, adsConfig, vipGate, analyticsLogger, adsPrefs);
  }
}
