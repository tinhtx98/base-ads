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
public final class IronSourceBiddingManager_Factory implements Factory<IronSourceBiddingManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  public IronSourceBiddingManager_Factory(Provider<Context> contextProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<AnalyticsLogger> analyticsLoggerProvider) {
    this.contextProvider = contextProvider;
    this.adsConfigProvider = adsConfigProvider;
    this.analyticsLoggerProvider = analyticsLoggerProvider;
  }

  @Override
  public IronSourceBiddingManager get() {
    return newInstance(contextProvider.get(), adsConfigProvider.get(), analyticsLoggerProvider.get());
  }

  public static IronSourceBiddingManager_Factory create(Provider<Context> contextProvider,
      Provider<AdsConfig> adsConfigProvider, Provider<AnalyticsLogger> analyticsLoggerProvider) {
    return new IronSourceBiddingManager_Factory(contextProvider, adsConfigProvider, analyticsLoggerProvider);
  }

  public static IronSourceBiddingManager newInstance(Context context, AdsConfig adsConfig,
      AnalyticsLogger analyticsLogger) {
    return new IronSourceBiddingManager(context, adsConfig, analyticsLogger);
  }
}
