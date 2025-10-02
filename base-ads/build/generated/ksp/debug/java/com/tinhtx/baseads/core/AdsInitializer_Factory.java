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
public final class AdsInitializer_Factory implements Factory<AdsInitializer> {
  private final Provider<AnalyticsLogger> analyticsLoggerProvider;

  private final Provider<AdsConfig> adsConfigProvider;

  public AdsInitializer_Factory(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider) {
    this.analyticsLoggerProvider = analyticsLoggerProvider;
    this.adsConfigProvider = adsConfigProvider;
  }

  @Override
  public AdsInitializer get() {
    return newInstance(analyticsLoggerProvider.get(), adsConfigProvider.get());
  }

  public static AdsInitializer_Factory create(Provider<AnalyticsLogger> analyticsLoggerProvider,
      Provider<AdsConfig> adsConfigProvider) {
    return new AdsInitializer_Factory(analyticsLoggerProvider, adsConfigProvider);
  }

  public static AdsInitializer newInstance(AnalyticsLogger analyticsLogger, AdsConfig adsConfig) {
    return new AdsInitializer(analyticsLogger, adsConfig);
  }
}
