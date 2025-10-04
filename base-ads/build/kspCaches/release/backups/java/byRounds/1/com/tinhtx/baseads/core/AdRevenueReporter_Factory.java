package com.tinhtx.baseads.core;

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
public final class AdRevenueReporter_Factory implements Factory<AdRevenueReporter> {
  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  public AdRevenueReporter_Factory(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider) {
    this.analyticsLoggerProvider = analyticsLoggerProvider;
    this.adsConfigProvider = adsConfigProvider;
  }

  @Override
  public AdRevenueReporter get() {
    return newInstance(analyticsLoggerProvider.get(), adsConfigProvider.get());
  }

  public static AdRevenueReporter_Factory create(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider) {
    return new AdRevenueReporter_Factory(analyticsLoggerProvider, adsConfigProvider);
  }

  public static AdRevenueReporter newInstance(AnalyticsLogger analyticsLogger,
      AdsConfig adsConfig) {
    return new AdRevenueReporter(analyticsLogger, adsConfig);
  }
}
