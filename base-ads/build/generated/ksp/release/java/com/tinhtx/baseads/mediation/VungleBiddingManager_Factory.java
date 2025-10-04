package com.tinhtx.baseads.mediation;

import android.content.Context;
import com.tinhtx.baseads.core.AdsConfig;
import com.tinhtx.baseads.core.AnalyticsLogger;
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
public final class VungleBiddingManager_Factory implements Factory<VungleBiddingManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  public VungleBiddingManager_Factory(Provider<Context> contextProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<AnalyticsLogger> analyticsLoggerProvider) {
    this.contextProvider = contextProvider;
    this.adsConfigProvider = adsConfigProvider;
    this.analyticsLoggerProvider = analyticsLoggerProvider;
  }

  @Override
  public VungleBiddingManager get() {
    return newInstance(contextProvider.get(), adsConfigProvider.get(), analyticsLoggerProvider.get());
  }

  public static VungleBiddingManager_Factory create(Provider<Context> contextProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<AnalyticsLogger> analyticsLoggerProvider) {
    return new VungleBiddingManager_Factory(contextProvider, adsConfigProvider, analyticsLoggerProvider);
  }

  public static VungleBiddingManager newInstance(Context context, AdsConfig adsConfig,
      AnalyticsLogger analyticsLogger) {
    return new VungleBiddingManager(context, adsConfig, analyticsLogger);
  }
}
