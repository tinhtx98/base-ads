package com.tinhtx.baseads.core;

import com.tinhtx.baseads.mediation.IronSourceBiddingManager;
import com.tinhtx.baseads.mediation.VungleBiddingManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class AdsInitializer_Factory implements Factory<AdsInitializer> {
  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  private final Provider<VungleBiddingManager> vungleBiddingManagerProvider;

  private final Provider<IronSourceBiddingManager> ironSourceBiddingManagerProvider;

  public AdsInitializer_Factory(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider,
      Provider<VungleBiddingManager> vungleBiddingManagerProvider,
      Provider<IronSourceBiddingManager> ironSourceBiddingManagerProvider) {
    this.analyticsLoggerProvider = analyticsLoggerProvider;
    this.adsConfigProvider = adsConfigProvider;
    this.vungleBiddingManagerProvider = vungleBiddingManagerProvider;
    this.ironSourceBiddingManagerProvider = ironSourceBiddingManagerProvider;
  }

  @Override
  public AdsInitializer get() {
    return newInstance(analyticsLoggerProvider.get(), adsConfigProvider.get(), vungleBiddingManagerProvider.get(), ironSourceBiddingManagerProvider.get());
  }

  public static AdsInitializer_Factory create(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider,
      Provider<VungleBiddingManager> vungleBiddingManagerProvider,
      Provider<IronSourceBiddingManager> ironSourceBiddingManagerProvider) {
    return new AdsInitializer_Factory(analyticsLoggerProvider, adsConfigProvider, vungleBiddingManagerProvider, ironSourceBiddingManagerProvider);
  }

  public static AdsInitializer newInstance(AnalyticsLogger analyticsLogger, AdsConfig adsConfig,
      VungleBiddingManager vungleBiddingManager,
      IronSourceBiddingManager ironSourceBiddingManager) {
    return new AdsInitializer(analyticsLogger, adsConfig, vungleBiddingManager, ironSourceBiddingManager);
  }
}
